package com.networknt.kafka.common;

import com.networknt.kafka.common.config.KafkaProducerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KafkaProducerConfigTest {
    @Test
    public void testLoadingDefaultConfigName() {
        KafkaProducerConfig config = KafkaProducerConfig.load();
        assertNotNull(config);
    }

    @Test
    public void testLoadingAdditionalProps() {
        KafkaProducerConfig config = KafkaProducerConfig.load("kafka-producer-additionalProps");
        assertNotNull(config);
        final var kafkaProps = config.getKafkaMapProperties();
        assertTrue(kafkaProps.containsKey("myNewProperty"));
        assertEquals("http://localhost:8081", kafkaProps.get("schema.registry.url"));
    }
}
