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
        Assertions.assertNotNull(config.getGroupId());
    }

    @Test
    public void testAdditionalProperties() {
        KafkaConsumerConfig config = (KafkaConsumerConfig) Config.getInstance().getJsonObjectConfig("test-additional-consumer-props", KafkaConsumerConfig.class);
        var mergedProps = config.getProperties();
        Assertions.assertNotNull(mergedProps);

        var bootstrap = mergedProps.get("bootstrap.servers");
        Assertions.assertNotNull(bootstrap);
        Assertions.assertEquals("localhost:9092", bootstrap);

        var customProp = mergedProps.get("my.other.prop");
        Assertions.assertNotNull(customProp);
        Assertions.assertEquals("someprop", customProp);
    }
}
