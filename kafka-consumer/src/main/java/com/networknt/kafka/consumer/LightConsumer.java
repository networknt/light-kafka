package com.networknt.kafka.consumer;

import com.networknt.kafka.common.config.KafkaConsumerConfig;

import java.util.ArrayList;
import java.util.List;

public interface LightConsumer {
    void open();
    void close();

    /**
     * Register the module to the Registry so that the config can be shown in the server/info
     *
     */
    default void registerModule() {
    }
}
