package com.networknt.kafka.common;

import com.networknt.config.Config;
import com.networknt.kafka.common.KafkaProducerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KafkaProducerConfigTest {
    @Test
    public void testLoadingConfig() {
        KafkaProducerConfig config = (KafkaProducerConfig) Config.getInstance().getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);
        assertNotNull(config);
    }
}
