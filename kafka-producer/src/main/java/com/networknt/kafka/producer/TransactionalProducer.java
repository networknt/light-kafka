package com.networknt.kafka.producer;

import com.networknt.config.Config;
import com.networknt.httpstring.HttpStringConstants;
import com.networknt.kafka.common.config.KafkaProducerConfig;
import com.networknt.kafka.common.TransactionalKafkaException;
import com.networknt.kafka.common.FlinkKafkaProducer;
import com.networknt.server.ServerConfig;

import com.networknt.utility.Constants;
import io.undertow.server.HttpServerExchange;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.InvalidTxnStateException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionalProducer implements Runnable, QueuedLightProducer {
    static private final Logger logger = LoggerFactory.getLogger(TransactionalProducer.class);
    static String callerId = "unknown";
    static final KafkaProducerConfig config = (KafkaProducerConfig) Config.getInstance().getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);
    static {
        if(config.isInjectCallerId()) {
            ServerConfig serverConfig = ServerConfig.getInstance();
            if(serverConfig != null) {
                callerId = serverConfig.getServiceId();
            }
        }
    }
    static final String topic = config.getTopic();

    private BlockingQueue<ProducerRecord<byte[], byte[]>> txQueue = new LinkedBlockingQueue();

    public BlockingQueue<ProducerRecord<byte[], byte[]>> getTxQueue() {
        return txQueue;
    }

    /**
     * Pool of available transactional ids.
     */
    private final BlockingDeque<String> availableTransactionalIds = new LinkedBlockingDeque<>();

    private KafkaTransactionState currentTransaction;

    /** The callback than handles error propagation or logging callbacks. */
    private transient Callback callback;

    /** Errors encountered in the async producer are stored here. */
    private transient volatile Exception asyncException;

    /** Tracking if the current transaction is about to timeout. */
    private volatile long transactionTimeout;

    /** Number of unacknowledged records. */
    private final AtomicLong pendingRecords = new AtomicLong();

    /** indicate the at the thread should be stopped */
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public TransactionalProducer() {
        logger.info("The TransactionalProducer is created");
    }

    public void run() {
        while(!stopped.get()) {
            try {
                //transactionTimeout = System.currentTimeMillis() + 840000; // 14 minutes timeout
                currentTransaction = beginTransaction();
                List<ProducerRecord<byte[], byte[]>> buffer = new ArrayList<>();
                while(!stopped.get()) {
                    int added = drain(txQueue, buffer, 5000, 1000, TimeUnit.MILLISECONDS);
                    if(logger.isTraceEnabled() && added > 0) logger.trace("drained transactions = " + added);
                    for(int j = 0; j < added; j++) {
                        ProducerRecord<byte[], byte[]> record = buffer.get(j);
                        invoke(currentTransaction, topic, record);
                    }
                    if(added > 0) break; // break the inner loop so that the batch transaction can be committed.
                }
                // if there is no input for a while, the producer id might be removed from the Kafka broker
                // in that case, we need to find out and recreate the producer.
                long producerId = currentTransaction.producer.getProducerId();
                short epoch = currentTransaction.producer.getEpoch();
                if(logger.isDebugEnabled()) logger.debug("producerId = " + producerId + " epoch = " + epoch);
                // The 15 minute default transaction timeout is a hard limit, even we can increase it, it won't help with
                // a big number. We need to resume the transaction to call the broker to do it after 10 minutes.
                commit(currentTransaction);
            } catch (InterruptedException e) {
                logger.error("InterruptedException", e);
            } catch (TransactionalKafkaException e) {
                logger.error("TransactionalKafkaException", e);
                abort(currentTransaction);
            } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
                // We can't recover from these exceptions, so our only option is to close the producer and exit.
                logger.error("One of the three exceptions",e);
                try {close();} catch (Exception ex) {ex.printStackTrace();}
            } catch (KafkaException e) {
                // For all other exceptions, just abort the transaction and try again.
                logger.error("KafkaException", e);
                abort(currentTransaction);
                if(e instanceof ConfigException) {
                    throw new RuntimeException("Kafka is down!");
                }
            }
        }
    }

    /**
     * Drains the queue as {@link BlockingQueue#drainTo(Collection, int)}, but if the requested
     * {@code numElements} elements are not available, it will wait for them up to the specified
     * timeout.
     *
     * @param q the blocking queue to be drained
     * @param <E> element
     * @param buffer where to add the transferred elements
     * @param numElements the number of elements to be waited for
     * @param timeout how long to wait before giving up, in units of {@code unit}
     * @param unit a {@code TimeUnit} determining how to interpret the timeout parameter
     * @return the number of elements transferred
     * @throws InterruptedException if interrupted while waiting
     */
    public static <E> int drain(
            BlockingQueue<E> q,
            Collection<? super E> buffer,
            int numElements,
            long timeout,
            TimeUnit unit)
            throws InterruptedException {
        /*
         * This code performs one System.nanoTime() more than necessary, and in return, the time to
         * execute Queue#drainTo is not added *on top* of waiting for the timeout (which could make
         * the timeout arbitrarily inaccurate, given a queue that is slow to drain).
         */
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        int added = 0;
        while (added < numElements) {
            // we could rely solely on #poll, but #drainTo might be more efficient when there are multiple
            // elements already available (e.g. LinkedBlockingQueue#drainTo locks only once)
            added += q.drainTo(buffer, numElements - added);
            if (added < numElements) { // not enough elements immediately available; will have to poll
                E e = q.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                if (e == null) {
                    break; // we already waited enough, and there are no more elements in sight
                }
                buffer.add(e);
                added++;
            }
        }
        return added;
    }

    public void invoke(KafkaTransactionState transaction, String topic, ProducerRecord<byte[], byte[]> record) throws TransactionalKafkaException {
        //ProducerRecord<byte[], byte[]> record;
        //record = new ProducerRecord<>(topic, partition, key.getBytes(StandardCharsets.UTF_8), value);
        pendingRecords.incrementAndGet();
        transaction.producer.send(record, callback);
    }

    /**
     * Initializes the connection to Kafka.
     */
    public void open() {
        callback = new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception e) {
                if (e != null) {
                    logger.error("Error while sending record to Kafka: " + e.getMessage(), e);
                }
                acknowledgeMessage();
            }
        };
    }

    protected void preCommit(KafkaTransactionState transaction) throws TransactionalKafkaException {
        flush(transaction);
    }

    protected void commit(KafkaTransactionState transaction) {
        transaction.producer.commitTransaction();
        recycleTransactionalProducer(transaction.producer);
    }

    protected void recoverAndCommit(KafkaTransactionState transaction) {
        try (FlinkKafkaProducer<byte[], byte[]> producer =
                     initTransactionalProducer(transaction.transactionalId)) {
            producer.resumeTransaction(transaction.producerId, transaction.epoch);
            producer.commitTransaction();
        }
        catch (InvalidTxnStateException | ProducerFencedException ex) {
            // That means we have committed this transaction before.
            logger.warn("Encountered error {} while recovering transaction {}. " +
                            "Presumably this transaction has been already committed before",
                    ex,
                    transaction);
        }
    }

    protected void abort(KafkaTransactionState transaction) {
        // If running from the unit test, the transaction might be null at this time as the server is gone
        if(transaction != null) {
            transaction.producer.abortTransaction();
            recycleTransactionalProducer(transaction.producer);
        }
    }

    private void recycleTransactionalProducer(FlinkKafkaProducer<byte[], byte[]> producer) {
        availableTransactionalIds.add(producer.getTransactionalId());
        producer.close();
    }

    protected void recoverAndAbort(KafkaTransactionState transaction) {
        try (FlinkKafkaProducer<byte[], byte[]> producer = initTransactionalProducer(transaction.transactionalId)) {
            producer.initTransactions();
        }
    }

    private void acknowledgeMessage() {
        pendingRecords.decrementAndGet();
    }


    public void close() {
        final KafkaTransactionState currentTransaction = currentTransaction();
        if (currentTransaction != null) {
            // to avoid exceptions on aborting transactions with some pending records
            flush(currentTransaction);
            commit(currentTransaction);
            // normal abort for AT_LEAST_ONCE and NONE do not clean up resources because of producer reusing, thus
            // we need to close it manually
        }
        stopped.getAndSet(true);
    }

    /**
     * Flush pending records.
     * @param transaction
     */
    private void flush(KafkaTransactionState transaction) {
        if (transaction.producer != null) {
            transaction.producer.flush();
        }
        long pendingRecordsCount = pendingRecords.get();
        if (pendingRecordsCount != 0) {
            throw new IllegalStateException("Pending record count must be zero at this point: " + pendingRecordsCount);
        }
    }

    public KafkaTransactionState currentTransaction() {
        return currentTransaction;
    }

    public KafkaTransactionState beginTransaction() throws TransactionalKafkaException {
        FlinkKafkaProducer<byte[], byte[]> producer = createTransactionalProducer();
        producer.beginTransaction();
        return new KafkaTransactionState(producer.getTransactionalId(), producer);
    }

    /**
     * For each checkpoint we create new {@link FlinkKafkaProducer} so that new transactions will not clash
     * with transactions created during previous checkpoints ({@code producer.initTransactions()} assures that we
     * obtain new producerId and epoch counters).
     */
    private FlinkKafkaProducer<byte[], byte[]> createTransactionalProducer() throws TransactionalKafkaException {
        FlinkKafkaProducer<byte[], byte[]> producer = initTransactionalProducer((String)config.getKafkaMapProperties().get("transactional.id"));
        producer.initTransactions();
        return producer;
    }

    private FlinkKafkaProducer<byte[], byte[]> initTransactionalProducer(String transactionalId) {
        config.getKafkaMapProperties().put("transactional.id", transactionalId);
        return initProducer();
    }

    private FlinkKafkaProducer<byte[], byte[]> initProducer() {
        FlinkKafkaProducer<byte[], byte[]> producer = new FlinkKafkaProducer<>(config.getKafkaMapProperties());
        logger.info("Starting FlinkKafkaProducer");
        return producer;
    }

    @Override
    public void propagateHeaders(ProducerRecord record, HttpServerExchange exchange) {
        Headers headers = record.headers();
        String token = exchange.getRequestHeaders().getFirst(Constants.AUTHORIZATION_STRING);
        headers.add(Constants.AUTHORIZATION_STRING, token.getBytes(StandardCharsets.UTF_8));
        String cid = exchange.getRequestHeaders().getFirst(HttpStringConstants.CORRELATION_ID);
        headers.add(Constants.CORRELATION_ID_STRING, cid.getBytes(StandardCharsets.UTF_8));
        String tid = exchange.getRequestHeaders().getFirst(HttpStringConstants.TRACEABILITY_ID);
        if(tid != null) {
            headers.add(Constants.TRACEABILITY_ID_STRING, tid.getBytes(StandardCharsets.UTF_8));
        }
        if(config.isInjectCallerId()) {
            headers.add(Constants.CALLER_ID_STRING, callerId.getBytes(StandardCharsets.UTF_8));
        }
        /*
        if(config.isInjectOpenTracing()) {
            Tracer tracer = exchange.getAttachment(AttachmentConstants.EXCHANGE_TRACER);
            if(tracer != null && tracer.activeSpan() != null) {
                Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_PRODUCER);
                Tags.MESSAGE_BUS_DESTINATION.set(tracer.activeSpan(), record.topic());
                tracer.inject(tracer.activeSpan().context(), Format.Builtin.TEXT_MAP, new KafkaProducerRecordCarrier(record));
            }
        } else {
            String cid = exchange.getRequestHeaders().getFirst(HttpStringConstants.CORRELATION_ID);
            headers.add(Constants.CORRELATION_ID_STRING, cid.getBytes(StandardCharsets.UTF_8));
            String tid = exchange.getRequestHeaders().getFirst(HttpStringConstants.TRACEABILITY_ID);
            if(tid != null) {
                headers.add(Constants.TRACEABILITY_ID_STRING, tid.getBytes(StandardCharsets.UTF_8));
            }
        }
        */
    }

    public static int addressToPartition(String address) {
        String bankId = address.substring(0, 4);
        return Integer.valueOf(bankId);
    }

    /**
     * State for handling transactions.
     */
    static class KafkaTransactionState {

        private final transient FlinkKafkaProducer<byte[], byte[]> producer;

        final String transactionalId;

        final long producerId;

        final short epoch;

        KafkaTransactionState(String transactionalId, FlinkKafkaProducer<byte[], byte[]> producer) {
            this(transactionalId, producer.getProducerId(), producer.getEpoch(), producer);
        }

        KafkaTransactionState(FlinkKafkaProducer<byte[], byte[]> producer) {
            this(null, -1, (short) -1, producer);
        }

        KafkaTransactionState(
                String transactionalId,
                long producerId,
                short epoch,
                FlinkKafkaProducer<byte[], byte[]> producer) {
            this.transactionalId = transactionalId;
            this.producerId = producerId;
            this.epoch = epoch;
            this.producer = producer;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s [transactionalId=%s, producerId=%s, epoch=%s]",
                    this.getClass().getSimpleName(),
                    transactionalId,
                    producerId,
                    epoch);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            KafkaTransactionState that = (KafkaTransactionState) o;

            if (producerId != that.producerId) {
                return false;
            }
            if (epoch != that.epoch) {
                return false;
            }
            return transactionalId != null ? transactionalId.equals(that.transactionalId) : that.transactionalId == null;
        }

        @Override
        public int hashCode() {
            int result = transactionalId != null ? transactionalId.hashCode() : 0;
            result = 31 * result + (int) (producerId ^ (producerId >>> 32));
            result = 31 * result + (int) epoch;
            return result;
        }
    }

    /**
     * Keep information required to deduce next safe to use transactional id.
     */
    public static class NextTransactionalIdHint {
        public int lastParallelism = 0;
        public long nextFreeTransactionalId = 0;

        public NextTransactionalIdHint() {
            this(0, 0);
        }

        public NextTransactionalIdHint(int parallelism, long nextFreeTransactionalId) {
            this.lastParallelism = parallelism;
            this.nextFreeTransactionalId = nextFreeTransactionalId;
        }
    }

    /**
     * Adds metadata (currently only the start time of the transaction) to the transaction object.
     */
    public static final class TransactionHolder<KafkaTransactionState> {

        private final TransactionalProducer.KafkaTransactionState handle;

        /**
         * The system time when {@link #handle} was created.
         * Used to determine if the current transaction has exceeded its timeout specified by
         */
        private final long transactionStartTime;

        public TransactionHolder(TransactionalProducer.KafkaTransactionState handle, long transactionStartTime) {
            this.handle = handle;
            this.transactionStartTime = transactionStartTime;
        }

        long elapsedTime(Clock clock) {
            return clock.millis() - transactionStartTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TransactionHolder<?> that = (TransactionHolder<?>) o;

            if (transactionStartTime != that.transactionStartTime) {
                return false;
            }
            return handle != null ? handle.equals(that.handle) : that.handle == null;
        }

        @Override
        public int hashCode() {
            int result = handle != null ? handle.hashCode() : 0;
            result = 31 * result + (int) (transactionStartTime ^ (transactionStartTime >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "TransactionHolder{" +
                    "handle=" + handle +
                    ", transactionStartTime=" + transactionStartTime +
                    '}';
        }
    }
}
