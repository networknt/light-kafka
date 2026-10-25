package com.networknt.kafka.producer;

import com.networknt.config.Jackson2Defaults;
import com.networknt.exception.FrameworkException;
import com.networknt.kafka.common.converter.AvroConverter;
import com.networknt.kafka.common.converter.ProtobufConverter;
import com.networknt.kafka.entity.EmbeddedFormat;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConfluentBoundaryTest {
    private static final String TOPIC = "events";
    private static final Map<String, Object> CONFIG = Map.of("schema.registry.url", "mock://phase6");
    private MockSchemaRegistryClient registry;
    private SchemaRecordSerializer serializer;

    @BeforeEach
    void setUp() {
        registry = (MockSchemaRegistryClient) MockSchemaRegistry.getClientForScope(UUID.randomUUID().toString(),
                List.of(new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()));
        serializer = new SchemaRecordSerializer(registry, CONFIG, CONFIG, CONFIG);
    }

    @AfterEach
    void resetRegistry() {
        registry.reset();
    }

    @Test
    void avroAcceptsJackson3InputAndReturnsItsOriginalRecord() throws Exception {
        AvroSchema schema = new AvroSchema("""
                {"type":"record","name":"Item","fields":[
                {"name":"name","type":"string"},{"name":"qty","type":"int"}]}
                """);
        JsonNode input = Jackson2Defaults.mapper().readTree("{\"name\":\"雪\",\"qty\":7}");
        byte[] bytes = serialize(EmbeddedFormat.AVRO, schema, input);
        try (KafkaAvroDeserializer decoder = new KafkaAvroDeserializer(registry)) {
            decoder.configure(CONFIG, false);
            assertEquals(input, new AvroConverter().toJson(decoder.deserialize(TOPIC, bytes)).getJson());
        }
    }

    @Test
    void jsonSchemaReceivesATreeRatherThanJackson3BeanProperties() throws Exception {
        JsonSchema schema = new JsonSchema("""
                {"type":"object","required":["name","qty"],"properties":{
                "name":{"type":"string"},"qty":{"type":"integer"},"nested":{"type":"array"}}}
                """);
        JsonNode input = Jackson2Defaults.mapper().readTree("""
                {"name":"雪","qty":7,"nested":[null,true,1.25,{"large":123456789012345678901234567890}]}
                """);
        byte[] bytes = serialize(EmbeddedFormat.JSONSCHEMA, schema, input);
        try (var decoder = new KafkaJsonSchemaDeserializer<com.fasterxml.jackson.databind.JsonNode>(
                registry, CONFIG, com.fasterxml.jackson.databind.JsonNode.class)) {
            var decoded = decoder.deserialize(TOPIC, bytes);
            assertEquals("雪", decoded.get("name").asText());
            assertEquals(7, decoded.get("qty").intValue());
            assertTrue(decoded.get("nested").get(0).isNull());
            assertTrue(decoded.get("nested").get(1).booleanValue());
            assertEquals(new java.math.BigDecimal("1.25"), decoded.get("nested").get(2).decimalValue());
            assertEquals(new java.math.BigInteger("123456789012345678901234567890"),
                    decoded.get("nested").get(3).get("large").bigIntegerValue());
        }
    }

    @Test
    void protobufAcceptsJackson3Input() throws Exception {
        ProtobufSchema schema = new ProtobufSchema("""
                syntax = "proto3";
                message Item { string name = 1; int32 qty = 2; }
                """);
        JsonNode input = Jackson2Defaults.mapper().readTree("{\"name\":\"雪\",\"qty\":7}");
        byte[] bytes = serialize(EmbeddedFormat.PROTOBUF, schema, input);
        try (var decoder = new KafkaProtobufDeserializer<Message>(registry)) {
            decoder.configure(CONFIG, false);
            assertEquals(input, new ProtobufConverter().toJson(decoder.deserialize(TOPIC, bytes)).getJson());
        }
    }

    @Test
    void invalidJsonSchemaDataKeepsFrameworkError() throws Exception {
        JsonSchema schema = new JsonSchema("{\"type\":\"integer\"}");
        var registered = register(schema);
        FrameworkException error = assertThrows(FrameworkException.class, () -> serializer.serialize(0,
                EmbeddedFormat.JSONSCHEMA, TOPIC, Optional.of(registered),
                Jackson2Defaults.mapper().readTree("\"not an integer\""), false));
        assertEquals("ERR12206", error.getStatus().getCode());
    }

    @Test
    void rawJsonRetainsCompactAndIndentedSerializerOptions() throws Exception {
        JsonNode input = Jackson2Defaults.mapper().readTree("{\"nested\":[null,true,1.25],\"text\":\"雪\"}");
        for (boolean indent : new boolean[]{false, true}) {
            Map<String, Object> config = Map.of("json.indent.output", indent);
            NoSchemaRecordSerializer raw = new NoSchemaRecordSerializer(config);
            var legacy = new io.confluent.kafka.serializers.KafkaJsonSerializer<com.fasterxml.jackson.databind.JsonNode>();
            legacy.configure(config, false);
            try {
                byte[] expected = legacy.serialize("", new com.fasterxml.jackson.databind.ObjectMapper().readTree(input.toString()));
                byte[] actual = raw.serialize(0, EmbeddedFormat.JSON, input).orElseThrow().toByteArray();
                assertArrayEquals(expected, actual);
            } finally {
                legacy.close();
            }
        }
    }

    @Test
    void schemalessNullStringAndBinaryKeepTheirContracts() {
        NoSchemaRecordSerializer raw = new NoSchemaRecordSerializer(Map.of());
        var mapper = Jackson2Defaults.mapper();
        assertTrue(raw.serialize(0, EmbeddedFormat.JSON, mapper.nullNode()).isEmpty());
        assertEquals("雪", raw.serialize(0, EmbeddedFormat.STRING, mapper.stringNode("雪"))
                .orElseThrow().toString(StandardCharsets.UTF_8));
        assertEquals(ByteString.copyFrom(new byte[]{0, 1, -1}),
                raw.serialize(0, EmbeddedFormat.BINARY, mapper.stringNode("AAH/")).orElseThrow());
        assertThrows(RuntimeException.class, () -> raw.serialize(3, EmbeddedFormat.BINARY, mapper.stringNode("!")));
        assertThrows(RuntimeException.class, () -> raw.serialize(3, EmbeddedFormat.STRING, mapper.createObjectNode()));
    }

    private RegisteredSchema register(ParsedSchema schema) throws Exception {
        int id = registry.register(TOPIC + "-value", schema);
        return new RegisteredSchema(TOPIC + "-value", id, 1, schema);
    }

    private byte[] serialize(EmbeddedFormat format, ParsedSchema schema, JsonNode input) throws Exception {
        byte[] bytes = serializer.serialize(0, format, TOPIC, Optional.of(register(schema)), input, false)
                .orElseThrow().toByteArray();
        assertEquals(0, bytes[0], "Retain the Confluent wire-format magic byte");
        return bytes;
    }
}
