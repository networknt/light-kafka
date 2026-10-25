package com.networknt.kafka.producer;

import com.networknt.config.Jackson2Defaults;
import com.networknt.exception.FrameworkException;
import com.networknt.kafka.entity.AuditRecord;
import com.networknt.kafka.entity.EmbeddedFormat;
import com.networknt.kafka.entity.ProduceRecord;
import com.networknt.kafka.entity.ProduceRequest;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Jackson3GoldenWireTest {
    private static final JsonMapper MAPPER = Jackson2Defaults.mapper();
    private static final List<String> CORPUS = List.of(
            "{\"text\":\"雪\",\"array\":[null,true,1.25,123456789012345678901234567890]}",
            "[1.0,-0.0,1.0E20]", "null", "42", "\"hello\"");
    private static final List<EmbeddedFormat> FORMATS = List.of(
            EmbeddedFormat.AVRO, EmbeddedFormat.JSONSCHEMA, EmbeddedFormat.PROTOBUF);
    private MockSchemaRegistryClient registry;

    @AfterEach
    void resetRegistry() {
        if (registry != null) registry.reset();
    }

    @TestFactory
    Stream<DynamicTest> matchesTheFrozenJackson2WireBaseline() throws IOException {
        try (var stream = Jackson3GoldenWireTest.class.getResourceAsStream("/jackson2-baseline/wire.txt")) {
            assertNotNull(stream, "Frozen Jackson 2 wire baseline is missing");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).lines().map(line -> {
                String[] record = line.split("\\|", 3);
                assertEquals("WIRE", record[0]);
                return DynamicTest.dynamicTest(record[1], () -> {
                    try {
                        assertEquals(record[2], actualWireValue(record[1]), record[1]);
                    } finally {
                        resetRegistry();
                    }
                });
            }).toList().stream();
        }
    }

    private String actualWireValue(String key) throws Exception {
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

    private String rawJson(String key) throws IOException {
        String[] parts = key.split("-");
        int index = Integer.parseInt(parts[2]);
        var serializer = new NoSchemaRecordSerializer(Map.of("json.indent.output", Boolean.parseBoolean(parts[1])));
        var value = serializer.serialize(index, EmbeddedFormat.JSON, MAPPER.readTree(CORPUS.get(index)));
        return value.isEmpty() ? "NULL" : encode(value.get().toByteArray());
    }

    private String rawValue(EmbeddedFormat format, String json) throws IOException {
        var serializer = new NoSchemaRecordSerializer(Map.of());
        return encode(serializer.serialize(0, format, MAPPER.readTree(json)).orElseThrow().toByteArray());
    }

    private String preciseDecimal() throws IOException {
        var precise = MAPPER.createObjectNode();
        precise.put("decimal", new BigDecimal("0.1234567890123456789012345678900"));
        var serializer = new NoSchemaRecordSerializer(Map.of());
        return encode(serializer.serialize(0, EmbeddedFormat.JSON, precise).orElseThrow().toByteArray());
    }

    private String auditJson() {
        var audit = new AuditRecord();
        audit.setId("fixed");
        audit.setTopic("events");
        audit.setTimestamp(123L);
        audit.setAuditType(AuditRecord.AuditType.ACTIVE_CONSUMER);
        audit.setAuditStatus(AuditRecord.AuditStatus.SUCCESS);
        return encode(MAPPER.writeValueAsBytes(audit));
    }

    private String produceRequestJson() {
        var record = ProduceRecord.create(MAPPER.readTree("1"), MAPPER.readTree(CORPUS.get(0)));
        record.setHeaders(Optional.of(Map.of("x", "y")));
        record.setTimestamp(Optional.of(123L));
        var request = new ProduceRequest();
        request.setRecords(List.of(record));
        return encode(MAPPER.writeValueAsBytes(request));
    }

    private SchemaRecordSerializer schemaSerializer() {
        registry = new MockSchemaRegistryClient(List.of(
                new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()));
        var config = Map.<String, Object>of("schema.registry.url", "mock://parity");
        return new SchemaRecordSerializer(registry, config, config, config);
    }

    private List<ParsedSchema> schemas() {
        return List.of(
                new AvroSchema("{\"type\":\"record\",\"name\":\"Item\",\"fields\":["
                        + "{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"qty\",\"type\":\"int\"}]}"),
                new JsonSchema("{\"type\":\"object\"}"),
                new ProtobufSchema("syntax=\"proto3\"; message Item {string name=1; int32 qty=2;}"));
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
                String input = i == 1 ? CORPUS.get(0) : "{\"name\":\"雪\",\"qty\":7}";
                result = encode(serializer.serialize(0, format, topic, Optional.of(schema),
                        MAPPER.readTree(input), false).orElseThrow().toByteArray());
            }
        }
        return result;
    }

    private String badBinary() {
        var serializer = new NoSchemaRecordSerializer(Map.of());
        var error = assertThrows(RuntimeException.class,
                () -> serializer.serialize(3, EmbeddedFormat.BINARY, MAPPER.readTree("\"!\"")));
        return error.getClass().getName() + "|" + error.getMessage();
    }

    private String badSchema() throws Exception {
        var serializer = schemaSerializer();
        var schema = new JsonSchema("{\"type\":\"integer\"}");
        int id = registry.register("bad-value", schema);
        var registered = new RegisteredSchema("bad-value", id, 1, schema);
        var error = assertThrows(FrameworkException.class, () -> serializer.serialize(0,
                EmbeddedFormat.JSONSCHEMA, "bad", Optional.of(registered), MAPPER.readTree("\"invalid\""), false));
        return error.getStatus().getCode();
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
