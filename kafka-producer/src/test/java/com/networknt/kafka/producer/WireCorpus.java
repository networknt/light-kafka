package com.networknt.kafka.producer;

import com.networknt.kafka.entity.EmbeddedFormat;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

/** Shared inputs only: expected bytes still come from original Jackson 2 serializers. */
final class WireCorpus {
    private WireCorpus() { }

    static final List<String> CORPUS = List.of(
            "{\"text\":\"雪\",\"array\":[null,true,1.25,123456789012345678901234567890]}",
            "[1.0,-0.0,1.0E20]", "null", "42", "\"hello\"");
    static final List<EmbeddedFormat> FORMATS = List.of(
            EmbeddedFormat.AVRO, EmbeddedFormat.JSONSCHEMA, EmbeddedFormat.PROTOBUF);

    static final List<String> WIRE_KEYS = List.of(
            "json-false-0", "json-false-1", "json-false-2", "json-false-3", "json-false-4",
            "json-true-0", "json-true-1", "json-true-2", "json-true-3", "json-true-4",
            "precise-decimal", "binary", "string", "audit", "produce-request",
            "AVRO", "JSONSCHEMA", "PROTOBUF", "bad-binary", "bad-schema");
    static final BigDecimal PRECISE_DECIMAL = new BigDecimal("0.1234567890123456789012345678900");
    static final String SCHEMA_RECORD = "{\"name\":\"雪\",\"qty\":7}";

    static List<ParsedSchema> schemas() {
        return List.of(
                new AvroSchema("{\"type\":\"record\",\"name\":\"Item\",\"fields\":["
                        + "{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"qty\",\"type\":\"int\"}]}"),
                new JsonSchema("{\"type\":\"object\"}"),
                new ProtobufSchema("syntax=\"proto3\"; message Item {string name=1; int32 qty=2;}"));
    }

    static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
