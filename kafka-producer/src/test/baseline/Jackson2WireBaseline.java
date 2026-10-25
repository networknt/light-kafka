package com.networknt.kafka.producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.protobuf.ByteString;
import com.networknt.kafka.entity.EmbeddedFormat;
import com.networknt.kafka.entity.ProduceRecord;
import java.util.Optional;

import static com.networknt.kafka.producer.WireCorpus.WIRE_KEYS;

public class Jackson2WireBaseline extends WireScenario<JsonNode> {
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new Jdk8Module());

    public static void main(String[] args) throws Exception {
        var generator = new Jackson2WireBaseline();
        for (String key : WIRE_KEYS) {
            try {
                System.out.println("WIRE|" + key + "|" + generator.actualWireValue(key));
            } finally {
                generator.resetRegistry();
            }
        }
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
