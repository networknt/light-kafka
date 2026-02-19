package com.networknt.kafka.common;

import com.networknt.kafka.common.config.KafkaConsumerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConsumerConfigTest {
    @Test
    void testLoadingDefaultConfigName() {
        KafkaConsumerConfig config = KafkaConsumerConfig.load();
        assertNotNull(config);
        assertNotNull(config.getProperties().getGroupId());
    }

    @Test
    void testLoadingLocalConfig() {
        KafkaConsumerConfig config = KafkaConsumerConfig.load("kafka-consumer-local");
        assertNotNull(config);
        final var kafkaProps = config.getKafkaMapProperties();
        assertTrue(kafkaProps.containsKey("group.id"));
        assertFalse(kafkaProps.containsKey("saslJaasConfig"));
        assertEquals("http://localhost:8081", kafkaProps.get("schema.registry.url"));
    }

    @Test
    void testLoadingAdditionalProps() {
        KafkaConsumerConfig config = KafkaConsumerConfig.load("kafka-consumer-additionalProps");
        assertNotNull(config);
        final var kafkaProps = config.getKafkaMapProperties();
        assertTrue(kafkaProps.containsKey("myNewProperty"));
        assertEquals("http://localhost:8081", kafkaProps.get("schema.registry.url"));
    }
}
