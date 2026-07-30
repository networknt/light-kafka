package com.networknt.kafka.common;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlinkKafkaProducerTest {

    @Test
    void resumeTransactionRestoresProducerIdentityWithKafka42Internals() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "resume-transaction-test");

        FlinkKafkaProducer<byte[], byte[]> producer = new FlinkKafkaProducer<>(properties);
        try {
            producer.resumeTransaction(42L, (short) 3);

            assertEquals(42L, producer.getProducerId());
            assertEquals((short) 3, producer.getEpoch());
        } finally {
            producer.close(Duration.ZERO);
        }
    }
}
