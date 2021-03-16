package com.networknt.kafka.producer;

import com.networknt.kafka.common.TransactionalKafkaException;
import io.undertow.server.HttpServerExchange;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * A generic Kafka producer that can handle the header propagation with open and close methods to be
 * called from the startup and shutdown hooks.
 *
 * @author Steve Hu
 */
public interface LightProducer {
    /**
     * This method is used to create an instance of producer in a startup hook for most of the producers.
     * Queued producer will start a background thread to process the queued message in batch and this
     * method can be used to start the thread in a startup hook.
     *
     */
    void open();

    /**
     * This method is called by a shutdown hook to close the producer if it is not null.
     */
    void close();
}
