package com.networknt.kafka.producer;

import com.networknt.config.Config;
import org.junit.Assert;
import org.junit.Test;

public class KafkaProducerConfigTest {
    @Test
    public void testLoadingConfig() {
        KafkaProducerConfig config = (KafkaProducerConfig) Config.getInstance().getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);
        Assert.assertNotNull(config);
    }
}
