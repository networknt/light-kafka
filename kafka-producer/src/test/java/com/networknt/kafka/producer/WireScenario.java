package com.networknt.kafka.producer;

import com.google.protobuf.ByteString;
import com.networknt.exception.FrameworkException;
import com.networknt.kafka.entity.AuditRecord;
import com.networknt.kafka.entity.EmbeddedFormat;
import com.networknt.kafka.entity.ProduceRecord;
import com.networknt.kafka.entity.ProduceRequest;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.networknt.kafka.producer.WireCorpus.CORPUS;
import static com.networknt.kafka.producer.WireCorpus.FORMATS;
import static com.networknt.kafka.producer.WireCorpus.SCHEMA_RECORD;
import static com.networknt.kafka.producer.WireCorpus.encode;
import static com.networknt.kafka.producer.WireCorpus.schemas;

/** One driver, compiled separately against the current and original serializers. */
abstract class WireScenario<N> {
    private MockSchemaRegistryClient registry;

    final void resetRegistry() {
        if (registry != null) registry.reset();
    }

    abstract N readTree(String json) throws Exception;
    abstract N decimalTree();
    abstract byte[] writeValueAsBytes(Object value) throws Exception;
    abstract ProduceRecord createRecord(N key, N value);
    abstract Optional<ByteString> serializeRaw(NoSchemaRecordSerializer serializer, int index,
                                               EmbeddedFormat format, N node) throws Exception;
    abstract Optional<ByteString> serializeSchema(SchemaRecordSerializer serializer, EmbeddedFormat format,
                                                  String topic, RegisteredSchema schema, N node) throws Exception;

    final String actualWireValue(String key) throws Exception {
        if (key.startsWith("json-")) return rawJson(key);
        if (FORMATS.stream().anyMatch(format -> format.name().equals(key))) {
            return schemaWireValue(EmbeddedFormat.valueOf(key));
        }
        return switch (key) {
            case "precise-decimal" -> preciseDecimal();
            case "binary" -> rawValue(EmbeddedFormat.BINARY, "\"AAH/\"");
            case "string" -> rawValue(EmbeddedFormat.STRING, "\"雪\"");
            case "audit" -> auditJson();
            case "produce-request" -> produceRequestJson();
            case "bad-binary" -> badBinary();
            case "bad-schema" -> badSchema();
            default -> throw new IllegalArgumentException("Unknown frozen wire key: " + key);
        };
    }

    private String rawJson(String key) throws Exception {
        String[] parts = key.split("-");
        int index = Integer.parseInt(parts[2]);
        var serializer = new NoSchemaRecordSerializer(Map.of("json.indent.output", Boolean.parseBoolean(parts[1])));
        var value = serializeRaw(serializer, index, EmbeddedFormat.JSON, readTree(CORPUS.get(index)));
        return value.isEmpty() ? "NULL" : encode(value.get().toByteArray());
    }

    private String rawValue(EmbeddedFormat format, String json) throws Exception {
        var serializer = new NoSchemaRecordSerializer(Map.of());
        return encode(serializeRaw(serializer, 0, format, readTree(json)).orElseThrow().toByteArray());
    }

    private String preciseDecimal() throws Exception {
        N precise = decimalTree();
        var serializer = new NoSchemaRecordSerializer(Map.of());
        return encode(serializeRaw(serializer, 0, EmbeddedFormat.JSON, precise).orElseThrow().toByteArray());
    }

    private String auditJson() throws Exception {
        var audit = new AuditRecord();
        audit.setId("fixed");
        audit.setTopic("events");
        audit.setTimestamp(123L);
        audit.setAuditType(AuditRecord.AuditType.ACTIVE_CONSUMER);
        audit.setAuditStatus(AuditRecord.AuditStatus.SUCCESS);
        return encode(writeValueAsBytes(audit));
    }

    private String produceRequestJson() throws Exception {
        var record = createRecord(readTree("1"), readTree(CORPUS.get(0)));
        record.setHeaders(Optional.of(Map.of("x", "y")));
        record.setTimestamp(Optional.of(123L));
        var request = new ProduceRequest();
        request.setRecords(List.of(record));
        return encode(writeValueAsBytes(request));
    }

    private SchemaRecordSerializer schemaSerializer() {
        registry = new MockSchemaRegistryClient(List.of(
                new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()));
        var config = Map.<String, Object>of("schema.registry.url", "mock://parity");
        return new SchemaRecordSerializer(registry, config, config, config);
    }

    private String schemaWireValue(EmbeddedFormat format) throws Exception {
        var serializer = schemaSerializer();
        var schemas = schemas();
        String result = null;
        // Register in baseline order so the exact wire IDs remain 1, 2 and 3.
        for (int i = 0; i < FORMATS.size(); i++) {
            String topic = "events" + i;
            String subject = topic + "-value";
            int id = registry.register(subject, schemas.get(i));
            if (FORMATS.get(i) == format) {
                var schema = new RegisteredSchema(subject, id, 1, schemas.get(i));
                String input = i == 1 ? CORPUS.get(0) : SCHEMA_RECORD;
                result = encode(serializeSchema(serializer, format, topic, schema, readTree(input)).orElseThrow().toByteArray());
            }
        }
        return result;
    }

    private String badBinary() throws Exception {
        var serializer = new NoSchemaRecordSerializer(Map.of());
        var error = expectFailure(RuntimeException.class,
                () -> serializeRaw(serializer, 3, EmbeddedFormat.BINARY, readTree("\"!\"")));
        return error.getClass().getName() + "|" + error.getMessage();
    }

    private String badSchema() throws Exception {
        var serializer = schemaSerializer();
        var schema = new JsonSchema("{\"type\":\"integer\"}");
        int id = registry.register("bad-value", schema);
        var registered = new RegisteredSchema("bad-value", id, 1, schema);
        var error = expectFailure(FrameworkException.class, () -> serializeSchema(serializer, EmbeddedFormat.JSONSCHEMA, "bad", registered, readTree("\"invalid\"")));
        return error.getStatus().getCode();
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }

    private static <T extends Throwable> T expectFailure(Class<T> type, ThrowingAction action) {
        try {
            action.run();
        } catch (Throwable failure) {
            if (type.isInstance(failure)) return type.cast(failure);
            throw new AssertionError("Unexpected failure type", failure);
        }
        throw new AssertionError("Expected " + type.getName());
    }

}
