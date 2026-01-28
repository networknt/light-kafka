package com.networknt.kafka.consumer;

import com.networknt.kafka.common.config.KafkaConsumerConfig;

import java.util.ArrayList;
import java.util.List;

public interface LightConsumer {
    void open();
    void close();
}
