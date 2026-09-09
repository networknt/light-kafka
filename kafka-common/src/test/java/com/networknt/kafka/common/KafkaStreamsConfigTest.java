package com.networknt.kafka.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KafkaStreamsConfigTest {
    @Test
    public void testLoadingDefaultConfigName() {
        KafkaStreamsConfig config = KafkaStreamsConfig.load();
        assertNotNull(config);
    }

    @Test
    public void testLoadingAdditionalProps() {
        KafkaStreamsConfig config     = KafkaStreamsConfig.load("kafka-streams-additionalProps");
        assertNotNull(config);
        final var kafkaProps = config.getKafkaMapProperties();
        assertTrue(kafkaProps.containsKey("myNewProperty"));
        assertEquals("inline-streams-value", kafkaProps.get("inline.custom.property"));
        assertFalse(kafkaProps.containsKey("additionalKafkaProperties"));
        assertEquals("http://localhost:8081", kafkaProps.get("schema.registry.url"));
    }

}
