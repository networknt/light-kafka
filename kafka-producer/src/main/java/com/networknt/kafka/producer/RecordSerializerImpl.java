package com.networknt.kafka.producer;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.ByteString;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import com.networknt.kafka.entity.EmbeddedFormat;

import java.util.HashMap;
import java.util.Optional;

public class RecordSerializerImpl implements RecordSerializer {
    private final NoSchemaRecordSerializer noSchemaRecordSerializer;
    private final SchemaRecordSerializer schemaRecordSerializer;
    public RecordSerializerImpl() {
        noSchemaRecordSerializer = new NoSchemaRecordSerializer(new HashMap<String, Object>());
        schemaRecordSerializer = new SchemaRecordSerializer(
                new CachedSchemaRegistryClient("http://localhost:8081", 100),
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
