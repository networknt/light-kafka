package com.networknt.kafka.common;

import com.networknt.config.Config;
import com.networknt.service.SingletonServiceFactory;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
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
        KafkaStreamsConfig streamsConfig = (KafkaStreamsConfig) Config.getInstance().getJsonObjectConfig(KafkaStreamsConfig.CONFIG_NAME, KafkaStreamsConfig.class);
        if(streamsConfig != null) {
            config.putAll(streamsConfig.getProperties());
        }
        if(config.size() == 0) {
            // try to use the kafka-producer.yml and use the properties
            KafkaProducerConfig producerConfig = (KafkaProducerConfig) Config.getInstance().getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);
            if(producerConfig != null) {
                config.putAll(producerConfig.getProperties());
            }
        }
        if(config.size() == 0) {
            // try to use kafka-consumer.yml and use the properties
            KafkaConsumerConfig consumerConfig = (KafkaConsumerConfig) Config.getInstance().getJsonObjectConfig(KafkaConsumerConfig.CONFIG_NAME, KafkaConsumerConfig.class);
            if(consumerConfig != null) {
                config.putAll(consumerConfig.getProperties());
            }
        }

        schemaRegistry = SingletonServiceFactory.getBean(SchemaRegistryClient.class);
    }
}
