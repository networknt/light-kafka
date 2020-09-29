package com.networknt.kafka.common;

import com.networknt.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KafkaConsumerConfigTest {
    @Test
    public void testLoadingConfig() {
        KafkaConsumerConfig config = (KafkaConsumerConfig) Config.getInstance().getJsonObjectConfig(KafkaConsumerConfig.CONFIG_NAME, KafkaConsumerConfig.class);
        Assertions.assertNotNull(config);
    }
}
