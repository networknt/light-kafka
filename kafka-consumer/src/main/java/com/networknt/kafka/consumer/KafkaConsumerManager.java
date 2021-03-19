/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.networknt.kafka.consumer;

import com.networknt.config.Config;
import com.networknt.exception.FrameworkException;
import com.networknt.kafka.common.converter.AvroConverter;
import com.networknt.kafka.common.KafkaConsumerConfig;
import com.networknt.kafka.common.converter.JsonSchemaConverter;
import com.networknt.kafka.common.converter.ProtobufConverter;
import com.networknt.kafka.entity.*;
import com.networknt.status.Status;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages consumer instances by mapping instance IDs to consumer objects, processing read requests,
 * and cleaning up when consumers disappear.
 *
 * <p>For read and commitOffsets tasks, it uses a {@link ThreadPoolExecutor}
 *  which spins up threads for handling read tasks.
 * Since read tasks do not complete on the first run but rather call the AK consumer's poll() method
 * continuously, we re-schedule them via a {@link DelayQueue}.
 * A {@link ReadTaskSchedulerThread} runs in a separate thread
 *  and re-submits the tasks to the executor.
 */
public class KafkaConsumerManager {

  private static final Logger log = LoggerFactory.getLogger(KafkaConsumerManager.class);
  private static final String CONSUMER_INSTANCE_NOT_FOUND = "ERR12200";
  private static final String CONSUMER_CONFIG_EXCEPTION = "ERR12201";
  private static final String CONSUMER_ALREADY_EXISTS = "ERR12202";
  private static final String INVALID_EMBEDDED_FORMAT = "ERR12203";
  private static final String CONSUMER_FORMAT_MISMATCH = "ERR12204";

  private final KafkaConsumerConfig config = (KafkaConsumerConfig) Config.getInstance().getJsonObjectConfig(KafkaConsumerConfig.CONFIG_NAME, KafkaConsumerConfig.class);
  private final Clock clock = Clock.systemUTC();

  // KafkaConsumerState is generic, but we store them untyped here. This allows many operations to
  // work without having to know the types for the consumer, only requiring type information
  // during read operations.
  private final Map<ConsumerInstanceId, KafkaConsumerState> consumers =
      new HashMap<ConsumerInstanceId, KafkaConsumerState>();
  // All kind of operations, like reading records, committing offsets and closing a consumer
  // are executed separately in dedicated threads via a cached thread pool.
  private final ExecutorService executor;
  private KafkaConsumerFactory consumerFactory;
  final DelayQueue<RunnableReadTask> delayedReadTasks = new DelayQueue<>();
  private final ExpirationThread expirationThread;
  private ReadTaskSchedulerThread readTaskSchedulerThread;

  private ConsumerInstanceId adminConsumerInstanceId = null;

  public KafkaConsumerManager(final KafkaConsumerConfig config) {

    // Cached thread pool
    int maxThreadCount = config.getMaxConsumerThreads();

    this.executor = new KafkaConsumerThreadPoolExecutor(0, maxThreadCount,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
              if (r instanceof ReadFutureTask) {
                RunnableReadTask readTask = ((ReadFutureTask)r).readTask;
                Duration retry = Duration.ofMillis(ThreadLocalRandom.current().nextInt(25, 76));
                log.debug(
                    "The runnable {} was rejected execution because the thread pool is saturated. "
                        + "Delaying execution for {}ms.",
                    r, retry.toMillis());
                readTask.delayFor(retry);
              } else {
                log.debug(
                    "The runnable {} was rejected execution because the thread pool is saturated. "
                        + "Executing on calling thread.",
                    r);
                // run commitOffset and consumer close tasks from the caller thread
                if (!executor.isShutdown()) {
                  r.run();
                }
              }
            }
          }
    );
    this.consumerFactory = null;
    this.expirationThread = new ExpirationThread();
    this.readTaskSchedulerThread = new ReadTaskSchedulerThread();
    this.expirationThread.start();
    this.readTaskSchedulerThread.start();
  }

  KafkaConsumerManager(KafkaConsumerConfig config, KafkaConsumerFactory consumerFactory) {
    this(config);
    this.consumerFactory = consumerFactory;
  }

  /**
   * Creates a new consumer instance and returns its unique ID.
   *
   * @param group Name of the consumer group to join
   * @param instanceConfig configuration parameters for the consumer
   * @return Unique consumer instance ID
   */
  public String createConsumer(String group, ConsumerInstanceConfig instanceConfig) {
    // The terminology here got mixed up for historical reasons, and remaining compatible moving
    // forward is tricky. To maintain compatibility, if the 'id' field is specified we maintain
    // the previous behavior of using it's value in both the URLs for the consumer (i.e. the
    // local name) and the ID (consumer.id setting in the consumer). Otherwise, the 'name' field
    // only applies to the local name. When we replace with the new consumer, we may want to
    // provide an alternate app name, or just reuse the name.
    String name = instanceConfig.getName();
    if (instanceConfig.getId() != null) { // Explicit ID request always overrides name
      name = instanceConfig.getId();
    }
    if (name == null) {
      name = "rest-consumer-";
      String serverId = config.getServerId();
      if (!serverId.isEmpty()) {
        name += serverId + "-";
      }
      name += UUID.randomUUID().toString();
    }

    ConsumerInstanceId cid = new ConsumerInstanceId(group, name);
    // Perform this check before
    synchronized (this) {
      if (consumers.containsKey(cid)) {
        Status status = new Status(CONSUMER_ALREADY_EXISTS, cid);
        throw new FrameworkException(status);
      } else {
        // Placeholder to reserve this ID
        consumers.put(cid, null);
      }
    }

    // Ensure we clean up the placeholder if there are any issues creating the consumer instance
    boolean succeeded = false;
    try {
      log.debug("Creating consumer " + name + " in group " + group);

      // Note the ordering here. We want to allow overrides, but almost all the
      // consumer-specific settings don't make sense to override globally (e.g. group ID, consumer
      // ID), and others we want to ensure get overridden (e.g. consumer.timeout.ms, which we
      // intentionally name differently in our own configs).
      //Properties props = (Properties) config.getOriginalProperties().clone();
      Properties props = config.getConsumerProperties();
      props.setProperty("group.id", group);
      // This ID we pass here has to be unique, only pass a value along if the deprecated ID field
      // was passed in. This generally shouldn't be used, but is maintained for compatibility.
      if (instanceConfig.getId() != null) {
        props.setProperty("consumer.id", instanceConfig.getId());
      }
      if (instanceConfig.getAutoCommitEnable() != null) {
        props.setProperty("enable.auto.commit", instanceConfig.getAutoCommitEnable());
      }
      if (instanceConfig.getAutoOffsetReset() != null) {
        props.setProperty("auto.offset.reset", instanceConfig.getAutoOffsetReset());
      }
      // override request.timeout.ms to the default
      // the consumer.request.timeout.ms setting given by the user denotes
      // how much time the proxy should wait before returning a response
      // and should not be propagated to the consumer
      props.setProperty("request.timeout.ms", "30000");

      switch (instanceConfig.getFormat()) {
        case AVRO:
          props.put("key.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
          props.put("value.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
          break;
        case JSONSCHEMA:
          props.put("key.deserializer",
              "io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer");
          props.put("value.deserializer",
              "io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer");
          break;
        case PROTOBUF:
          props.put("key.deserializer",
              "io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer");
          props.put("value.deserializer",
              "io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer");
          break;
        case JSON:
        case BINARY:
        default:
          props.put(
              "key.deserializer",
              "org.apache.kafka.common.serialization.ByteArrayDeserializer"
          );
          props.put(
              "value.deserializer",
              "org.apache.kafka.common.serialization.ByteArrayDeserializer"
          );
      }

      Consumer consumer;
      try {
        if (consumerFactory == null) {
          consumer = new KafkaConsumer(props);
        } else {
          consumer = consumerFactory.createConsumer(props);
        }
      } catch (ConfigException e) {
        Status status = new Status(CONSUMER_CONFIG_EXCEPTION, e.getMessage());
        throw new FrameworkException(status);
      }

      KafkaConsumerState state = createConsumerState(instanceConfig, cid, consumer);
      synchronized (this) {
        consumers.put(cid, state);
      }
      succeeded = true;
      return name;
    } finally {
      if (!succeeded) {
        synchronized (this) {
          consumers.remove(cid);
        }
      }
    }
  }

  private KafkaConsumerState createConsumerState(
          ConsumerInstanceConfig instanceConfig,
          ConsumerInstanceId cid, Consumer consumer
  ) throws FrameworkException {
    switch (instanceConfig.getFormat()) {
      case BINARY:
        return new BinaryKafkaConsumerState(config, instanceConfig, cid, consumer);
      case AVRO:
        return new SchemaKafkaConsumerState(
            config, instanceConfig, cid, consumer, new AvroConverter());
      case JSON:
        return new JsonKafkaConsumerState(config, instanceConfig, cid, consumer);
      case JSONSCHEMA:
        return new SchemaKafkaConsumerState(
            config, instanceConfig, cid, consumer, new JsonSchemaConverter());
      case PROTOBUF:
        return new SchemaKafkaConsumerState(
            config, instanceConfig, cid, consumer, new ProtobufConverter());
      default:
        Status status = new Status(INVALID_EMBEDDED_FORMAT, instanceConfig.getFormat());
        throw new FrameworkException(status);
    }
  }

  // The parameter consumerStateType works around type erasure, allowing us to verify at runtime
  // that the KafkaConsumerState we looked up is of the expected type and will therefore contain the
  // correct decoders
  public <KafkaKeyT, KafkaValueT, ClientKeyT, ClientValueT> void readRecords(
      final String group,
      final String instance,
      Class<? extends KafkaConsumerState<KafkaKeyT, KafkaValueT, ClientKeyT, ClientValueT>>
          consumerStateType,
      final Duration timeout,
      final long maxBytes,
      final ConsumerReadCallback<ClientKeyT, ClientValueT> callback
  ) {
    final KafkaConsumerState state;
    try {
      state = getConsumerInstance(group, instance);
    } catch (FrameworkException e) {
      callback.onCompletion(null, e);
      return;
    }

    if (!consumerStateType.isInstance(state)) {
      Status status = new Status(CONSUMER_FORMAT_MISMATCH);
      callback.onCompletion(null, new FrameworkException(status));
      return;
    }

    final KafkaConsumerReadTask<?, ?, ?, ?> task =
        new KafkaConsumerReadTask<KafkaKeyT, KafkaValueT, ClientKeyT, ClientValueT>(
            state, timeout, maxBytes, callback, config);
    executor.submit(new RunnableReadTask(new ReadTaskState(task, state, callback), config));
  }

  private class ReadFutureTask<V> extends FutureTask<V> {

    private final RunnableReadTask readTask;

    private ReadFutureTask(RunnableReadTask runnable, V result) {
      super(runnable, result);
      this.readTask = runnable;
    }
  }

  class KafkaConsumerThreadPoolExecutor extends ThreadPoolExecutor {
    private KafkaConsumerThreadPoolExecutor(int corePoolSize,
                                            int maximumPoolSize,
                                            long keepAliveTime,
                                            TimeUnit unit,
                                            BlockingQueue<Runnable> workQueue,
                                            RejectedExecutionHandler handler) {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
      if (runnable instanceof RunnableReadTask) {
        return new ReadFutureTask<>((RunnableReadTask) runnable, value);
      }
      return super.newTaskFor(runnable, value);
    }
  }

  class RunnableReadTask implements Runnable, Delayed {
    private final ReadTaskState taskState;
    private final Instant started;
    private final Instant requestExpiration;
    private final Duration backoff;
    // Expiration if this task is waiting, considering both the expiration of the whole task and
    // a single backoff, if one is in progress
    private Instant waitExpiration;
    private final Clock clock = Clock.systemUTC();

    public RunnableReadTask(ReadTaskState taskState, KafkaConsumerConfig config) {
      this.taskState = taskState;
      this.started = clock.instant();
      this.requestExpiration =
          this.started.plus(Duration.ofMillis(config.getRequestTimeoutMs()));
      this.backoff =
          Duration.ofMillis(config.getIteratorBackoffMs());
      this.waitExpiration = Instant.EPOCH;
    }

    /**
     * Delays for a minimum of {@code delayMs} or until the read request expires
     */
    public void delayFor(Duration delay) {
      if (!requestExpiration.isAfter(clock.instant())) {
        // no need to delay if the request has expired
        taskState.task.finish();
        log.trace("Finished executing  consumer read task ({}) due to request expiry",
            taskState.task);
        return;
      }

      Instant delayTo = clock.instant().plus(delay);
      waitExpiration = Collections.min(Arrays.asList(delayTo, requestExpiration));
      // add to delayedReadTasks so the scheduler thread can re-schedule another partial read later
      delayedReadTasks.add(this);
    }

    @Override
    public String toString() {
      return String.format("RunnableReadTask consumer id: %s; Read task: %s; "
              + "Request expiration time: %s; Wait expiration: %s",
          taskState.consumerState.getId(), taskState.task, requestExpiration, waitExpiration);
    }

    @Override
    public void run() {
      try {
        log.trace("Executing consumer read task ({})", taskState.task);

        taskState.task.doPartialRead();
        taskState.consumerState.updateExpiration();
        if (!taskState.task.isDone()) {
          delayFor(this.backoff);
        } else {
          log.trace("Finished executing consumer read task ({})", taskState.task);
        }
      } catch (FrameworkException e) {
        log.error("Failed to read records from consumer {} while executing read task ({}). {}",
                  taskState.consumerState.getId().toString(), taskState.task, e);
        taskState.callback.onCompletion(null, e);
      }
    }

    @Override
    public long getDelay(TimeUnit unit) {
      Duration delay = Duration.between(clock.instant(), waitExpiration);
      return unit.convert(delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
      if (o == null) {
        throw new NullPointerException("Delayed comparator cannot compare with null");
      }
      long otherObjDelay = o.getDelay(TimeUnit.MILLISECONDS);
      long delay = this.getDelay(TimeUnit.MILLISECONDS);

      return Long.compare(delay, otherObjDelay);
    }
  }

  public interface CommitCallback {

    public void onCompletion(List<TopicPartitionOffset> offsets, Exception e);
  }

  public Future commitOffsets(
      String group, String instance, final String async,
      final ConsumerOffsetCommitRequest offsetCommitRequest, final CommitCallback callback
  ) {
    final KafkaConsumerState state;
    try {
      state = getConsumerInstance(group, instance);
    } catch (RuntimeException e) {
      callback.onCompletion(null, e);
      return null;
    }

    return executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          List<TopicPartitionOffset> offsets = state.commitOffsets(async, offsetCommitRequest);
          callback.onCompletion(offsets, null);
        } catch (Exception e) {
          log.error("Failed to commit offsets for consumer " + state.getId().toString(), e);
          callback.onCompletion(null, e);
        } finally {
          state.updateExpiration();
        }
      }

      @Override
      public String toString() {
        return String.format("OffsetCommit consumer id: %s; Async: %s;",
            state.getId(), async);
      }
    });
  }

  public ConsumerCommittedResponse committed(
      String group,
      String instance,
      ConsumerCommittedRequest request
  ) {
    log.debug("Committed offsets for consumer " + instance + " in group " + group);
    ConsumerCommittedResponse response;
    KafkaConsumerState state = getConsumerInstance(group, instance);
    if (state != null) {
      response = state.committed(request);
    } else {
      response = new ConsumerCommittedResponse(new ArrayList<>());
    }
    return response;
  }

  /**
   * Returns the beginning offset of the {@code topic} {@code partition}.
   */
  public long getBeginningOffset(String topic, int partition) {
    log.debug("Beginning offset for topic {} and partition {}.", topic, partition);
    KafkaConsumerState<?, ?, ?, ?> consumer = getAdminConsumerInstance();
    return consumer.getBeginningOffset(topic, partition);
  }

  /**
   * Returns the end offset of the {@code topic} {@code partition}.
   */
  public long getEndOffset(String topic, int partition) {
    log.debug("End offset for topic {} and partition {}.", topic, partition);
    KafkaConsumerState<?, ?, ?, ?> consumer = getAdminConsumerInstance();
    return consumer.getEndOffset(topic, partition);
  }

  /**
   * Returns the earliest offset whose timestamp is greater than or equal to the given {@code
   * timestamp} in the {@code topic} {@code partition}, or empty if such offset does not exist.
   */
  public Optional<Long> getOffsetForTime(
      String topic, int partition, Instant timestamp) {
    log.debug("Offset for topic {} and partition {} at timestamp {}.", topic, partition, timestamp);
    KafkaConsumerState<?, ?, ?, ?> consumer = getAdminConsumerInstance();
    return consumer.getOffsetForTime(topic, partition, timestamp);
  }

  private String createAdminConsumerGroup() {
    String serverId = config.getServerId();
    if (serverId.isEmpty()) {
      return String.format("rest-consumer-group-%s", UUID.randomUUID().toString());
    } else {
      return String.format("rest-consumer-group-%s-%s", serverId, UUID.randomUUID().toString());
    }
  }

  private synchronized KafkaConsumerState<?, ?, ?, ?> getAdminConsumerInstance() {
    // Consumers expire when not used for some time. They can also be explicitly deleted by a user
    // using DELETE /consumers/{consumerGroup}/instances/{consumerInstances}. If the consumer does
    // not exist, create a new one.
    if (adminConsumerInstanceId == null || !consumers.containsKey(adminConsumerInstanceId)) {
      adminConsumerInstanceId = createAdminConsumerInstance();
    }
    return getConsumerInstance(adminConsumerInstanceId);
  }

  private ConsumerInstanceId createAdminConsumerInstance() {
    String consumerGroup = createAdminConsumerGroup();
    String consumerInstance = createConsumer(consumerGroup, ConsumerInstanceConfig.create(
        EmbeddedFormat.BINARY));
    return new ConsumerInstanceId(consumerGroup, consumerInstance);
  }

  public void deleteConsumer(String group, String instance) {
    log.debug("Destroying consumer " + instance + " in group " + group);
    final KafkaConsumerState state = getConsumerInstance(group, instance, true);
    state.close();
  }

  public void subscribe(String group, String instance, ConsumerSubscriptionRecord subscription) {
    log.debug("Subscribing consumer " + instance + " in group " + group);
    KafkaConsumerState state = getConsumerInstance(group, instance);
    if (state != null) {
      state.subscribe(subscription);
    }
  }

  public void unsubscribe(String group, String instance) {
    log.debug("Unsubcribing consumer " + instance + " in group " + group);
    KafkaConsumerState state = getConsumerInstance(group, instance);
    if (state != null) {
      state.unsubscribe();
    }
  }

  public ConsumerSubscriptionResponse subscription(String group, String instance) {
    KafkaConsumerState<?, ?, ?, ?> state = getConsumerInstance(group, instance);
    if (state != null) {
      return new ConsumerSubscriptionResponse(new ArrayList<>(state.subscription()));
    } else {
      return new ConsumerSubscriptionResponse(new ArrayList<>());
    }
  }

  public void seekToBeginning(String group, String instance, ConsumerSeekToRequest seekToRequest) {
    log.debug("seeking to beginning " + instance + " in group " + group);
    KafkaConsumerState state = getConsumerInstance(group, instance);
    if (state != null) {
      state.seekToBeginning(seekToRequest);
    }
  }

  public void seekToEnd(String group, String instance, ConsumerSeekToRequest seekToRequest) {
    log.debug("seeking to end " + instance + " in group " + group);
    KafkaConsumerState state = getConsumerInstance(group, instance);
    if (state != null) {
      state.seekToEnd(seekToRequest);
    }
  }

  public void seek(String group, String instance, ConsumerSeekRequest request) {
    log.debug("seeking to offset " + instance + " in group " + group);
    KafkaConsumerState<?, ?, ?, ?> state = getConsumerInstance(group, instance);
    if (state != null) {
      state.seek(request);
    }
  }

  public void assign(String group, String instance, ConsumerAssignmentRequest assignmentRequest) {
    log.debug("seeking to end " + instance + " in group " + group);
    KafkaConsumerState state = getConsumerInstance(group, instance);
    if (state != null) {
      state.assign(assignmentRequest);
    }
  }

  public ConsumerAssignmentResponse assignment(String group, String instance) {
    log.debug("getting assignment for  " + instance + " in group " + group);
    Vector<com.networknt.kafka.entity.TopicPartition> partitions = new Vector<>();
    KafkaConsumerState state = getConsumerInstance(group, instance);
    if (state != null) {
      java.util.Set<TopicPartition> topicPartitions = state.assignment();
      for (TopicPartition t : topicPartitions) {
        partitions.add(
            new com.networknt.kafka.entity.TopicPartition(t.topic(), t.partition())
        );
      }
    }
    return new ConsumerAssignmentResponse(partitions);
  }


  public void shutdown() {
    log.debug("Shutting down consumers");
    executor.shutdown();
    // Expiration thread needs to be able to acquire a lock on the KafkaConsumerManager to make sure
    // the shutdown will be able to complete.
    log.trace("Shutting down consumer expiration thread");
    expirationThread.shutdown();
    readTaskSchedulerThread.shutdown();
    synchronized (this) {
      for (Map.Entry<ConsumerInstanceId, KafkaConsumerState> entry : consumers.entrySet()) {
        entry.getValue().close();
      }
      consumers.clear();
      executor.shutdown();
    }
  }

  /**
   * Gets the specified consumer instance or throws a not found exception. Also removes the
   * consumer's expiration timeout so it is not cleaned up mid-operation.
   */
  private synchronized KafkaConsumerState<?, ?, ?, ?> getConsumerInstance(
      String group,
      String instance,
      boolean toRemove
  ) {
    ConsumerInstanceId id = new ConsumerInstanceId(group, instance);
    final KafkaConsumerState state = toRemove ? consumers.remove(id) : consumers.get(id);
    if (state == null) {
      Status status = new Status(CONSUMER_INSTANCE_NOT_FOUND, group, instance);
      throw new FrameworkException(status);
    }
    state.updateExpiration();
    return state;
  }

  KafkaConsumerState<?, ?, ?, ?> getConsumerInstance(String group, String instance) {
    return getConsumerInstance(group, instance, false);
  }

  private KafkaConsumerState<?, ?, ?, ?> getConsumerInstance(
      ConsumerInstanceId consumerInstanceId) {
    return getConsumerInstance(consumerInstanceId.getGroup(), consumerInstanceId.getInstance());
  }

  public interface KafkaConsumerFactory {

    Consumer createConsumer(Properties props);
  }

  private static class ReadTaskState {
    final KafkaConsumerReadTask task;
    final KafkaConsumerState consumerState;
    final ConsumerReadCallback callback;

    public ReadTaskState(KafkaConsumerReadTask task,
                         KafkaConsumerState state,
                         ConsumerReadCallback callback) {

      this.task = task;
      this.consumerState = state;
      this.callback = callback;
    }
  }

  private class ReadTaskSchedulerThread extends Thread {
    final AtomicBoolean isRunning = new AtomicBoolean(true);
    final CountDownLatch shutdownLatch = new CountDownLatch(1);

    ReadTaskSchedulerThread() {
      super("Read Task Scheduler Thread");
      setDaemon(true);
    }

    @Override
    public void run() {
      try {
        while (isRunning.get()) {
          RunnableReadTask readTask = delayedReadTasks.poll(500, TimeUnit.MILLISECONDS);
          if (readTask != null) {
            executor.submit(readTask);
          }
        }
      } catch (InterruptedException e) {
        // Interrupted by other thread, do nothing to allow this thread to exit
      } finally {
        shutdownLatch.countDown();
      }
    }

    public void shutdown() {
      try {
        isRunning.set(false);
        this.interrupt();
        shutdownLatch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException("Interrupted when shutting down read task scheduler thread.");
      }
    }
  }

  private class ExpirationThread extends Thread {

    AtomicBoolean isRunning = new AtomicBoolean(true);
    CountDownLatch shutdownLatch = new CountDownLatch(1);

    public ExpirationThread() {
      super("Consumer Expiration Thread");
      setDaemon(true);
    }

    @Override
    public void run() {
      try {
        while (isRunning.get()) {
          synchronized (KafkaConsumerManager.this) {
            Instant now = clock.instant();
            Iterator itr = consumers.values().iterator();
            while (itr.hasNext()) {
              final KafkaConsumerState state = (KafkaConsumerState) itr.next();
              if (state != null && state.expired(now)) {
                log.debug("Removing the expired consumer {}", state.getId());
                itr.remove();
                executor.submit(new Runnable() {
                  @Override
                  public void run() {
                    state.close();
                  }
                });
              }
            }
          }

          Thread.sleep(1000);
        }
      } catch (InterruptedException e) {
        // Interrupted by other thread, do nothing to allow this thread to exit
      }
      shutdownLatch.countDown();
    }

    public void shutdown() {
      try {
        isRunning.set(false);
        this.interrupt();
        shutdownLatch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException("Interrupted when shutting down expiration thread.");
      }
    }
  }
}
