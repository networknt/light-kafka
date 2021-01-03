package com.networknt.kafka.producer;

import com.networknt.kafka.common.TransactionalKafkaException;
import io.undertow.server.HttpServerExchange;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.concurrent.BlockingQueue;

/**
 * This is the guaranteed producer to ensure that the send is acknowledged from the Kafka brokers.
 *
 * @author Steve Hu
 */
public class SafeProducer implements Runnable, LightProducer {


    @Override
    public void open() {

    }

    @Override
    public void propagateHeaders(ProducerRecord record, HttpServerExchange exchange) {

    }

    @Override
    public BlockingQueue getTxQueue() {
        return null;
    }

    @Override
    public void close() throws TransactionalKafkaException {

    }

    @Override
    public void run() {

    }
}
