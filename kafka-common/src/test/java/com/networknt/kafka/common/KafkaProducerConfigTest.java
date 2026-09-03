package com.networknt.kafka.common;

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
        assertEquals("inline-producer-value", kafkaProps.get("inline.custom.property"));
        assertFalse(kafkaProps.containsKey("additionalKafkaProperties"));
        assertEquals("http://localhost:8081", kafkaProps.get("schema.registry.url"));
    }
}
