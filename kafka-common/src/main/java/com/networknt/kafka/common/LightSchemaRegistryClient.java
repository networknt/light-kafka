package com.networknt.kafka.common;

import com.networknt.config.Config;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.schemaregistry.client.rest.Versions;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * This is a singleton shared SchemaRegistryClient that is shared by consumer, producer and streams.
 * It has a default constructor so that the instance will be created based on the streams, producer
 * and consumer configuration in the sequence.
 *
 * It will be defined in the service.yml file and loaded by the first time it is used. The main goal
 * we want to create this class is to share the same instance between producer, consumer and streams
 * as we don't want to create multiple instances. Also, we want to make sure that the TLS and Auth
 * are handled gracefully in the default constructor.
 *
 * @author Steve Hu
 */
public class LightSchemaRegistryClient extends CachedSchemaRegistryClient {
    private static final Map<String, Object> configs;
    private static final String url;
    private static Integer cache = 100; // use 100 as default value, and it will be overwritten with the config.

    static {
        configs = new HashMap<>();
        // try to locate the kafka-streams.yml and use the properties
        KafkaStreamsConfig streamsConfig = (KafkaStreamsConfig) Config.getInstance().getJsonObjectConfig(KafkaStreamsConfig.CONFIG_NAME, KafkaStreamsConfig.class);
        if(streamsConfig != null) {
            configs.putAll(streamsConfig.getProperties());
        }
        if(configs.size() == 0) {
            // try to use the kafka-producer.yml and use the properties
            KafkaProducerConfig producerConfig = (KafkaProducerConfig) Config.getInstance().getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);
            if(producerConfig != null) {
                configs.putAll(producerConfig.getProperties());
            }
        }
        if(configs.size() == 0) {
            // try to use kafka-consumer.yml and use the properties
            KafkaConsumerConfig consumerConfig = (KafkaConsumerConfig) Config.getInstance().getJsonObjectConfig(KafkaConsumerConfig.CONFIG_NAME, KafkaConsumerConfig.class);
            if(consumerConfig != null) {
                configs.putAll(consumerConfig.getProperties());
            }
        }
        url = (String) configs.get("schema.registry.url");
        Object cacheObj = configs.get("schema.registry.cache");
        if (cacheObj != null && cacheObj instanceof String) {
            cache = Integer.valueOf((String) cacheObj);
        }
    }

    public LightSchemaRegistryClient() {
        super(
            new RestService(singletonList(url)),
            cache,
            Arrays.asList(new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()),
            configs,
           null
        );
    }
}
