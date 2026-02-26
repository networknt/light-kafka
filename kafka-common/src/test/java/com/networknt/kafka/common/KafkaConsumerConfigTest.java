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

    @Test
    void testLoadingSaslJaasConfig() {
        KafkaConsumerConfig config = KafkaConsumerConfig.load("kafka-consumer-sasl-jaas-config");
        assertNotNull(config);
        final var kafkaProps = config.getKafkaMapProperties();
        assertTrue(kafkaProps.containsKey("sasl.jaas.config"));
        assertEquals("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"username\" password=\")(*&_p@5Sw0Rd_^%$#\";", kafkaProps.get("sasl.jaas.config"));
    }
}
