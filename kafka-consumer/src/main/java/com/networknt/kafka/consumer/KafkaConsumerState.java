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

import com.networknt.kafka.common.KafkaConsumerConfig;
import com.networknt.kafka.entity.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import javax.ws.rs.InternalServerErrorException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * Tracks all the state for a consumer. This class is abstract in order to support multiple
 * serialization formats. Implementations must provide decoders and a method to convert
 * {@code KafkaMessageAndMetadata<K,V>} values to ConsumerRecords that can be returned to the client
 * (including translation if the decoded Kafka consumer type and ConsumerRecord types differ).
 */
public abstract class KafkaConsumerState<KafkaKeyT, KafkaValueT, ClientKeyT, ClientValueT> {

  private ConsumerInstanceId instanceId;
  private Consumer<KafkaKeyT, KafkaValueT> consumer;
  private final Clock clock = Clock.systemUTC();
  private final Duration consumerInstanceTimeout;
  private final ConsumerInstanceConfig consumerInstanceConfig;

  private final Queue<ConsumerRecord<KafkaKeyT, KafkaValueT>> consumerRecords = new ArrayDeque<>();

  volatile Instant expiration;

  KafkaConsumerState(
      KafkaConsumerConfig config,
      ConsumerInstanceConfig consumerInstanceConfig,
      ConsumerInstanceId instanceId,
      Consumer<KafkaKeyT, KafkaValueT> consumer
  ) {
    this.instanceId = instanceId;
    this.consumer = consumer;
    this.consumerInstanceTimeout =
        Duration.ofMillis(config.getInstanceTimeoutMs());
    this.expiration = clock.instant().plus(consumerInstanceTimeout);
    this.consumerInstanceConfig = consumerInstanceConfig;
  }

  public ConsumerInstanceId getId() {
    return instanceId;
  }

  public ConsumerInstanceConfig getConsumerInstanceConfig() {
    return consumerInstanceConfig;
  }

  /**
   * Converts a MessageAndMetadata using the Kafka decoder types into a ConsumerRecord using the
   * client's requested types. While doing so, computes the approximate size of the message in
   * bytes, which is used to track the approximate total payload size for consumer read responses to
   * determine when to trigger the response.
   * @param msg the message
   * @return consumer record and size
   */
  public abstract ConsumerRecordAndSize<ClientKeyT, ClientValueT> createConsumerRecord(
      ConsumerRecord<KafkaKeyT, KafkaValueT> msg
  );

  /**
   * Commit the given list of offsets
   * @param async the async flag
   * @param offsetCommitRequest the offset commit request
   * @return a list of TopicPartitionOffset
   */
  public synchronized List<TopicPartitionOffset> commitOffsets(
      boolean async,
      ConsumerOffsetCommitRequest offsetCommitRequest
  ) {
    // If no offsets are given, then commit all the records read so far
    if (offsetCommitRequest == null) {
      if (async) {
        consumer.commitAsync();
      } else {
        consumer.commitSync();
      }
    } else {
      Map<TopicPartition, OffsetAndMetadata> offsetMap =
          new HashMap<TopicPartition, OffsetAndMetadata>();

      //commit each given offset
      for (TopicPartitionOffsetMetadata t : offsetCommitRequest.getOffsets()) {
        if (t.getMetadata() == null) {
          offsetMap.put(
              new TopicPartition(t.getTopic(), t.getPartition()),
              new OffsetAndMetadata(t.getOffset() + 1)
          );
        } else {
          offsetMap.put(
              new TopicPartition(t.getTopic(), t.getPartition()),
              new OffsetAndMetadata(t.getOffset() + 1, t.getMetadata())
          );
        }

      }
      consumer.commitSync(offsetMap);
    }
    List<TopicPartitionOffset> result = new Vector<TopicPartitionOffset>();
    return result;
  }

  /**
   * Seek to the first offset for each of the given partitions.
   * @param seekToRequest the seek to request
   */
  public synchronized void seekToBeginning(ConsumerSeekToRequest seekToRequest) {
    if (seekToRequest != null) {
      Vector<TopicPartition> topicPartitions = new Vector<TopicPartition>();

      for (com.networknt.kafka.entity.TopicPartition t : seekToRequest.getPartitions()) {
        topicPartitions.add(new TopicPartition(t.getTopic(), t.getPartition()));
      }
      consumer.seekToBeginning(topicPartitions);
    }
  }

  /**
   * Seek to the last offset for each of the given partitions.
   * @param seekToRequest the seek to request
   */
  public synchronized void seekToEnd(ConsumerSeekToRequest seekToRequest) {
    if (seekToRequest != null) {
      Vector<TopicPartition> topicPartitions = new Vector<TopicPartition>();

      for (com.networknt.kafka.entity.TopicPartition t : seekToRequest.getPartitions()) {
        topicPartitions.add(new TopicPartition(t.getTopic(), t.getPartition()));
      }
      consumer.seekToEnd(topicPartitions);
    }
  }

  /**
   * Overrides the fetch offsets that the consumer will use on the next poll(timeout).
   * @param request the consumer seek request
   */
  public synchronized void seek(ConsumerSeekRequest request) {
    if (request == null) {
      return;
    }

    for (ConsumerSeekRequest.PartitionOffset partition : request.getOffsets()) {
      consumer.seek(
          new TopicPartition(partition.getTopic(), partition.getPartition()),
          new OffsetAndMetadata(partition.getOffset(), partition.getMetadata()));
    }

    Map<TopicPartition, String> metadata =
        request.getTimestamps().stream()
            .collect(
                Collectors.toMap(
                    partition ->
                        new TopicPartition(partition.getTopic(), partition.getPartition()),
                    ConsumerSeekRequest.PartitionTimestamp::getMetadata));

    Map<TopicPartition, OffsetAndTimestamp> offsets =
        consumer.offsetsForTimes(
            request.getTimestamps().stream()
                .collect(
                    Collectors.toMap(
                        partition ->
                            new TopicPartition(partition.getTopic(), partition.getPartition()),
                        partition -> partition.getTimestamp().toEpochMilli())));

    for (Map.Entry<TopicPartition, OffsetAndTimestamp> offset : offsets.entrySet()) {
      consumer.seek(
          offset.getKey(),
          new OffsetAndMetadata(
              offset.getValue().offset(), metadata.get(offset.getKey())));
    }
  }

  /**
   * Manually assign a list of partitions to this consumer.
   * @param assignmentRequest the assignment request
   */
  public synchronized void assign(ConsumerAssignmentRequest assignmentRequest) {
    if (assignmentRequest != null) {
      Vector<TopicPartition> topicPartitions = new Vector<TopicPartition>();

      for (com.networknt.kafka.entity.TopicPartition t
          : assignmentRequest.getPartitions()) {
        topicPartitions.add(new TopicPartition(t.getTopic(), t.getPartition()));
      }
      consumer.assign(topicPartitions);
    }
  }

  /**
   * Close the consumer,
   */
  public synchronized void close() {
    if (consumer != null) {
      consumer.close();
    }
    // Marks this state entry as no longer valid because the consumer group is being destroyed.
    consumer = null;
  }

  /**
   * Subscribe to the given list of topics to get dynamically assigned partitions.
   * @param subscription the subscription
   */
  public synchronized void subscribe(ConsumerSubscriptionRecord subscription) {
    if (subscription == null) {
      return;
    }

    if (consumer != null) {
      if (subscription.getTopics() != null) {
        consumer.subscribe(subscription.getTopics());
      } else if (subscription.getTopicPattern() != null) {
        Pattern topicPattern = Pattern.compile(subscription.getTopicPattern());
        NoOpOnRebalance noOpOnRebalance = new NoOpOnRebalance();
        consumer.subscribe(topicPattern, noOpOnRebalance);
      }
    }
  }

  /**
   * Unsubscribe from topics currently subscribed with subscribe(Collection).
   *
   */
  public synchronized void unsubscribe() {
    if (consumer != null) {
      consumer.unsubscribe();
    }
  }

  /**
   * Get the current list of topics subscribed.
   * @return a set of String
   */
  public synchronized Set<String> subscription() {
    Set<String> currSubscription = null;
    if (consumer != null) {
      currSubscription = consumer.subscription();
    }
    return currSubscription;
  }

  /**
   * Get the set of partitions currently assigned to this consumer.
   * @return a set of TopicPartition
   */
  public synchronized Set<TopicPartition> assignment() {
    Set<TopicPartition> currAssignment = null;
    if (consumer != null) {
      currAssignment = consumer.assignment();
    }
    return currAssignment;
  }


  /**
   * Get the last committed offset for the given partition (whether the commit happened by
   * this process or another).
   * @param request the request
   * @return ConsumerCommittedResponse
   */
  public synchronized ConsumerCommittedResponse committed(ConsumerCommittedRequest request) {
    Vector<TopicPartitionOffsetMetadata> offsets = new Vector<>();
    if (consumer != null) {
      for (com.networknt.kafka.entity.TopicPartition t : request.getPartitions()) {
        TopicPartition partition = new TopicPartition(t.getTopic(), t.getPartition());
        OffsetAndMetadata offsetMetadata = consumer.committed(partition);
        if (offsetMetadata != null) {
          offsets.add(
              new TopicPartitionOffsetMetadata(
                  partition.topic(),
                  partition.partition(),
                  offsetMetadata.offset(),
                  offsetMetadata.metadata()
              )
          );
        }
      }
    }
    return new ConsumerCommittedResponse(offsets);
  }

  /**
   * Returns the beginning offset of the {@code topic} {@code partition}.
   */
  synchronized long getBeginningOffset(String topic, int partition) {
    if (consumer == null) {
      throw new IllegalStateException("KafkaConsumerState has been closed.");
    }

    Map<TopicPartition, Long> response =
        consumer.beginningOffsets(singletonList(new TopicPartition(topic, partition)));

    if (response.size() != 1) {
      throw new InternalServerErrorException(
          String.format("Expected one offset, but got %d instead.", response.size()));
    }

    return response.values().stream().findAny().get();
  }

  /**
   * Returns the end offset of the {@code topic} {@code partition}.
   */
  synchronized long getEndOffset(String topic, int partition) {
    if (consumer == null) {
      throw new IllegalStateException("KafkaConsumerState has been closed.");
    }

    Map<TopicPartition, Long> response =
        consumer.endOffsets(singletonList(new TopicPartition(topic, partition)));

    if (response.size() != 1) {
      throw new InternalServerErrorException(
          String.format("Expected one offset, but got %d instead.", response.size()));
    }

    return response.values().stream().findAny().get();
  }

  /**
   * Returns the earliest offset whose timestamp is greater than or equal to the given {@code
   * timestamp} in the {@code topic} {@code partition}, or empty if such offset does not exist.
   */
  synchronized Optional<Long> getOffsetForTime(String topic, int partition, Instant timestamp) {
    if (consumer == null) {
      throw new IllegalStateException("KafkaConsumerState has been closed.");
    }

    Map<TopicPartition, OffsetAndTimestamp> response =
        consumer.offsetsForTimes(
            singletonMap(new TopicPartition(topic, partition), timestamp.toEpochMilli()));

    if (response.size() != 1) {
      throw new InternalServerErrorException(
          String.format("Expected one offset, but got %d instead.", response.size()));
    }

    return response.values().stream()
        .filter(Objects::nonNull)
        .findAny()
        .map(OffsetAndTimestamp::offset);
  }

  public synchronized boolean expired(Instant now) {
    return !expiration.isAfter(now);
  }

  public synchronized void updateExpiration() {
    this.expiration = clock.instant().plus(consumerInstanceTimeout);
  }

  synchronized ConsumerRecord<KafkaKeyT, KafkaValueT> peek() {
    return consumerRecords.peek();
  }

  synchronized boolean hasNext() {
    if (hasNextCached()) {
      return true;
    }
    // If none are available, try checking for any records already fetched by the consumer.
    getOrCreateConsumerRecords();

    return hasNextCached();
  }

  synchronized boolean hasNextCached() {
    return !consumerRecords.isEmpty();
  }

  synchronized ConsumerRecord<KafkaKeyT, KafkaValueT> next() {
    return consumerRecords.poll();
  }

  /**
   * Initiate poll(0) request to retrieve consumer records that are available immediately, or return
   * the existing
   * consumer records if the records have not been fully consumed by client yet. Must be
   * invoked with the lock held, i.e. after startRead().
   */
  private synchronized void getOrCreateConsumerRecords() {
    ConsumerRecords<KafkaKeyT, KafkaValueT> polledRecords = consumer.poll(0);
    //drain the iterator and buffer to list
    for (ConsumerRecord<KafkaKeyT, KafkaValueT> consumerRecord : polledRecords) {
      consumerRecords.add(consumerRecord);
    }
  }

  private class NoOpOnRebalance implements ConsumerRebalanceListener {

    public NoOpOnRebalance() {
    }

    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    }

    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
    }
  }

  protected Map<String, String> convertHeaders(Headers headers) {
    Map<String, String> headerMap = new HashMap<>();
    Iterator<Header> headerIterator = headers.iterator();
    while(headerIterator.hasNext()) {
      Header header = headerIterator.next();
      headerMap.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
    }
    return headerMap;
  }
}

