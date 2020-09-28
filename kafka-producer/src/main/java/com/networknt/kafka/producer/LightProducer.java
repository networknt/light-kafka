package com.networknt.kafka.producer;

import com.networknt.kafka.common.TransactionalKafkaException;

import java.util.concurrent.BlockingQueue;

public interface LightProducer {
    void open();
    BlockingQueue getTxQueue();
    void close() throws TransactionalKafkaException;
}
