package com.networknt.kafka.consumer;

import com.networknt.config.Config;
import org.junit.Assert;
import org.junit.Test;

public class KafkaConsumerConfigTest {
    @Test
    public void testLoadingConfig() {
        KafkaConsumerConfig config = (KafkaConsumerConfig) Config.getInstance().getJsonObjectConfig(KafkaConsumerConfig.CONFIG_NAME, KafkaConsumerConfig.class);
        Assert.assertNotNull(config);
    }
}
