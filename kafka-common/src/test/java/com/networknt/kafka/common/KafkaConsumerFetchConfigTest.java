package com.networknt.kafka.common;

import com.networknt.config.ConfigInjection;
import com.networknt.config.Config;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaConsumerFetchConfigTest {
    @Test
    void loadsPartitionFetchDefaultAndHonorsExternalOverride() {
        String key = "max.partition.fetch.bytes";
        String overrideKey = "kafka-consumer." + key;
        Map<String, Object> original = ConfigInjection.getDecryptedValueMap();
        Map<String, Object> overrides = original == null ? new HashMap<>() : new HashMap<>(original);
        try {
            overrides.remove(overrideKey);
            ConfigInjection.setDecryptedValueMap(overrides);
            Config.getInstance().clearConfigCache("kafka-consumer");
            assertEquals(102400, new KafkaConsumerConfig("kafka-consumer").getKafkaMapProperties().get(key));
            overrides.put(overrideKey, 65536);
            ConfigInjection.setDecryptedValueMap(overrides);
            Config.getInstance().clearConfigCache("kafka-consumer");
            assertEquals(65536, new KafkaConsumerConfig("kafka-consumer").getKafkaMapProperties().get(key));
        } finally {
            ConfigInjection.setDecryptedValueMap(original);
            Config.getInstance().clearConfigCache("kafka-consumer");
        }
    }
}
