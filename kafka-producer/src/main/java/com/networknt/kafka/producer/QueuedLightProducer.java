package com.networknt.kafka.producer;

import java.util.concurrent.BlockingQueue;

/**
 * Queued producer that can cache multiple messages in a queue and send them in a batch for maximum
 * throughput. It improves the performance; however, if the server is crashed before the batch is
 * sent out, the consumer doesn't know if the message is sent to the Kafka or not. A poll/push method
 * on the consumer should be established to accept notification if message is pushed to Kafka cluster.
 *
 * @author Steve Hu
 */
public interface QueuedLightProducer extends LightProducer {
    /**
     * Queued producer will start a background thread to process the queued message in batch and this
     * method can be used to start the thread in a startup hook.
     *
     */
    void open();

    /**
     * Get the blocking queue that is used to cache messages so that they can be sent in a batch.
     *
     * @return the queue that contains the cached messages
     */
    BlockingQueue getTxQueue();
}
