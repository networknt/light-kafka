package com.networknt.kafka.common;

import com.networknt.kafka.common.config.KafkaConsumerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KafkaConsumerConfigTest {
    @Test
    public void testLoadingDefaultConfigName() {
        KafkaConsumerConfig config = KafkaConsumerConfig.load();
        assertNotNull(config);
        assertNotNull(config.getProperties().getGroupId());
    }

    @Test
    public void testLoadingAdditionalProps() {
        KafkaConsumerConfig config = KafkaConsumerConfig.load("kafka-consumer-additionalProps");
        assertNotNull(config);
        final var kafkaProps = config.getKafkaMapProperties();
        assertTrue(kafkaProps.containsKey("myNewProperty"));
        assertEquals("http://localhost:8081", kafkaProps.get("schema.registry.url"));
    }
}
