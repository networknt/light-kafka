package com.networknt.kafka.producer;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.ByteString;
import com.networknt.config.Config;
import com.networknt.kafka.common.KafkaProducerConfig;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import com.networknt.kafka.entity.EmbeddedFormat;

import java.util.HashMap;
import java.util.Optional;

public class RecordSerializerImpl implements RecordSerializer {

    private final NoSchemaRecordSerializer noSchemaRecordSerializer;
    private final SchemaRecordSerializer schemaRecordSerializer;
    public RecordSerializerImpl() {
        KafkaProducerConfig config = (KafkaProducerConfig) Config.getInstance().getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);
        noSchemaRecordSerializer = new NoSchemaRecordSerializer(new HashMap<>());
        schemaRecordSerializer = new SchemaRecordSerializer(
                new CachedSchemaRegistryClient(config.getSchemaRegistryUrl(), config.getSchemaRegistryCache()),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>());
    }

    @Override
    public Optional<ByteString> serialize(
            EmbeddedFormat format,
            String topicName,
            Optional<RegisteredSchema> schema,
            JsonNode data,
            boolean isKey) {
        if (format.requiresSchema()) {
            return schemaRecordSerializer.serialize(format, topicName, schema, data, isKey);
        } else {
            return noSchemaRecordSerializer.serialize(format, data);
        }
    }

}
