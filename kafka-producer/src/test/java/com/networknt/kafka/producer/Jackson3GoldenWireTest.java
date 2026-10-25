package com.networknt.kafka.producer;

import com.google.protobuf.ByteString;
import com.networknt.config.Jackson2Defaults;
import com.networknt.kafka.entity.EmbeddedFormat;
import com.networknt.kafka.entity.ProduceRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static com.networknt.kafka.producer.WireCorpus.WIRE_KEYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Jackson3GoldenWireTest extends WireScenario<JsonNode> {
    private static final JsonMapper MAPPER = Jackson2Defaults.mapper();

    @TestFactory
    Stream<DynamicTest> matchesTheFrozenJackson2WireBaseline() throws IOException {
        try (var stream = Jackson3GoldenWireTest.class.getResourceAsStream("/jackson2-baseline/wire.txt")) {
            assertNotNull(stream, "Frozen Jackson 2 wire baseline is missing");
            Map<String, String> expected = parseBaseline(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            return WIRE_KEYS.stream().map(key -> DynamicTest.dynamicTest(key, () -> {
                try {
                    assertEquals(expected.get(key), actualWireValue(key), key);
                } finally {
                    resetRegistry();
                }
            }));
        }
    }

    private static Map<String, String> parseBaseline(String text) {
        Map<String, String> records = new LinkedHashMap<>();
        for (String line : text.lines().toList()) {
            String[] record = line.split("\\|", 3);
            assertEquals(3, record.length, "Malformed baseline record: " + line);
            assertEquals("WIRE", record[0]);
            assertEquals(null, records.putIfAbsent(record[1], record[2]), "Duplicate WIRE key: " + record[1]);
        }
        assertEquals(Set.copyOf(WIRE_KEYS), records.keySet(), "Frozen baseline must contain exactly the 20 WIRE keys");
        return records;
    }

    @Test
    void rejectsMissingDuplicateAndUnknownBaselineKeys() {
        assertThrows(AssertionError.class, () -> parseBaseline(""));
        assertThrows(AssertionError.class, () -> parseBaseline("WIRE|audit"));
        String complete = WIRE_KEYS.stream().map(key -> "WIRE|" + key + "|value").collect(java.util.stream.Collectors.joining("\n"));
        assertThrows(AssertionError.class, () -> parseBaseline(complete.substring(complete.indexOf('\n') + 1)));
        assertThrows(AssertionError.class, () -> parseBaseline(complete + "\nWIRE|audit|duplicate"));
        assertThrows(AssertionError.class, () -> parseBaseline(complete + "\nWIRE|unknown|value"));
    }

    @Override
    JsonNode readTree(String json) throws Exception {
        return MAPPER.readTree(json);
    }

    @Override
    JsonNode decimalTree() {
        return MAPPER.createObjectNode().put("decimal", WireCorpus.PRECISE_DECIMAL);
    }

    @Override
    byte[] writeValueAsBytes(Object value) throws Exception {
        return MAPPER.writeValueAsBytes(value);
    }

    @Override
    ProduceRecord createRecord(JsonNode key, JsonNode value) {
        return ProduceRecord.create(key, value);
    }

    @Override
    Optional<ByteString> serializeRaw(NoSchemaRecordSerializer serializer, int index,
                                       EmbeddedFormat format, JsonNode node) throws Exception {
        return serializer.serialize(index, format, node);
    }

    @Override
    Optional<ByteString> serializeSchema(SchemaRecordSerializer serializer, EmbeddedFormat format,
                                          String topic, RegisteredSchema schema, JsonNode node) throws Exception {
        return serializer.serialize(0, format, topic, Optional.of(schema), node, false);
    }
}
