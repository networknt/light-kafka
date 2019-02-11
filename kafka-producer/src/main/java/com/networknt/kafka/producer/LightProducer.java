package com.networknt.kafka.producer;

import java.util.concurrent.BlockingQueue;

public interface LightProducer {
    void open();
    BlockingQueue getTxQueue();
    void close() throws TransactionalKafkaException;
}
