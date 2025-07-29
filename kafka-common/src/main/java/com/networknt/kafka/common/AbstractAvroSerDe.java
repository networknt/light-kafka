package com.networknt.kafka.common;

import com.networknt.config.Config;
import com.networknt.kafka.common.config.KafkaConsumerConfig;
import com.networknt.kafka.common.config.KafkaProducerConfig;
import com.networknt.kafka.common.config.KafkaStreamsConfig;
import com.networknt.service.SingletonServiceFactory;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;

import java.util.HashMap;
import java.util.Map;

public class AbstractAvroSerDe {
    protected static final byte MAGIC_BYTE = 0x0;
    protected static final int idSize = 4;
    protected SchemaRegistryClient schemaRegistry;
    protected Map<String, Object> config;

    public AbstractAvroSerDe() {
        config = new HashMap<>();

        // try to locate the kafka-streams.yml and use the properties
        KafkaStreamsConfig streamsConfig = KafkaStreamsConfig.load();
        config.putAll(streamsConfig.getKafkaMapProperties());

        if (config.isEmpty()) {
            // try to use the kafka-producer.yml and use the properties
            KafkaProducerConfig producerConfig = KafkaProducerConfig.load();
            config.putAll(producerConfig.getKafkaMapProperties());
        }

        if (config.isEmpty()) {
            // try to use kafka-consumer.yml and use the properties
            KafkaConsumerConfig consumerConfig = KafkaConsumerConfig.load();
            config.putAll(consumerConfig.getKafkaMapProperties());
        }

        schemaRegistry = SingletonServiceFactory.getBean(SchemaRegistryClient.class);
    }
}
