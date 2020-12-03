package com.networknt.kafka.producer;

import com.networknt.kafka.common.TransactionalKafkaException;
import io.undertow.server.HttpServerExchange;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.concurrent.BlockingQueue;

public interface LightProducer {
    void open();
    void propagateHeaders(ProducerRecord record, HttpServerExchange exchange);
    BlockingQueue getTxQueue();
    void close() throws TransactionalKafkaException;
}
