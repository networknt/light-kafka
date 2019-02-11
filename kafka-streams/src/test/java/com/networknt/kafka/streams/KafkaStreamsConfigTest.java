package com.networknt.kafka.streams;

import com.networknt.config.Config;
import org.junit.Assert;
import org.junit.Test;

public class KafkaStreamsConfigTest {
    @Test
    public void testLoadingConfig() {
        KafkaStreamsConfig config = (KafkaStreamsConfig) Config.getInstance().getJsonObjectConfig(KafkaStreamsConfig.CONFIG_NAME, KafkaStreamsConfig.class);
        Assert.assertNotNull(config);
    }

}
