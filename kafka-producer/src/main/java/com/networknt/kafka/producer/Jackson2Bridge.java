package com.networknt.kafka.producer;

import com.networknt.config.Jackson2Defaults;
import tools.jackson.databind.JsonNode;

import java.io.IOException;

/** Byte-only conversion for Confluent 8.3.2; Jackson 2 types never enter public producer APIs. */
final class Jackson2Bridge {
    private static final com.fasterxml.jackson.databind.ObjectMapper CONFLUENT_MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper(com.fasterxml.jackson.core.JsonFactory.builder()
                    .streamReadConstraints(com.fasterxml.jackson.core.StreamReadConstraints.builder()
                            .maxStringLength(Integer.MAX_VALUE).maxNameLength(Integer.MAX_VALUE)
                            .maxNumberLength(Integer.MAX_VALUE).build()).build());
    // These parsers only consume bytes emitted from existing trees. Do not add
    // input-parser size restrictions that the original tree serializer did not have.
    // Request/config mappers and both writers retain their original constraints.
    private static final tools.jackson.core.TokenStreamFactory TREE_FACTORY =
            Jackson2Defaults.mapper().tokenStreamFactory().rebuild()
                    .streamReadConstraints(Jackson2Defaults.mapper().tokenStreamFactory().streamReadConstraints().rebuild()
                            .maxStringLength(Integer.MAX_VALUE).maxNameLength(Integer.MAX_VALUE)
                            .maxNumberLength(Integer.MAX_VALUE).build()).build();

    // Only explicit decimal nodes use these readers. Ordinary doubles keep their
    // original representation, including signed zero and exponent notation.
    private static final com.fasterxml.jackson.databind.ObjectReader LEGACY_DECIMAL_READER =
            CONFLUENT_MAPPER.readerFor(com.fasterxml.jackson.databind.JsonNode.class)
                    .with(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                    .without(com.fasterxml.jackson.databind.cfg.JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES);
    private static final tools.jackson.databind.ObjectReader DECIMAL_READER =
            Jackson2Defaults.mapper().readerFor(JsonNode.class)
                    .with(tools.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                    .without(tools.jackson.databind.cfg.JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES);

    private Jackson2Bridge() { }

    static com.fasterxml.jackson.databind.JsonNode toJackson2(JsonNode node) throws IOException {
        return preserveDecimals(node, CONFLUENT_MAPPER.readTree(Jackson2Defaults.mapper().writeValueAsBytes(node)));
    }

    static JsonNode toJackson3(com.fasterxml.jackson.databind.JsonNode node) throws IOException {
        return preserveDecimals(node, readJackson3(CONFLUENT_MAPPER.writeValueAsBytes(node), false));
    }

    private static com.fasterxml.jackson.databind.JsonNode preserveDecimals(
            JsonNode source, com.fasterxml.jackson.databind.JsonNode target) throws IOException {
        if (source.isBigDecimal()) {
            return LEGACY_DECIMAL_READER.readTree(Jackson2Defaults.mapper().writeValueAsBytes(source));
        }
        if (source.isObject()) {
            var object = (com.fasterxml.jackson.databind.node.ObjectNode) target;
            for (var field : source.properties()) {
                object.set(field.getKey(), preserveDecimals(field.getValue(), object.get(field.getKey())));
            }
        } else if (source.isArray()) {
            var array = (com.fasterxml.jackson.databind.node.ArrayNode) target;
            for (int i = 0; i < source.size(); i++) array.set(i, preserveDecimals(source.get(i), array.get(i)));
        }
        return target;
    }

    private static JsonNode preserveDecimals(com.fasterxml.jackson.databind.JsonNode source, JsonNode target)
            throws IOException {
        if (source.isBigDecimal()) {
            return readJackson3(CONFLUENT_MAPPER.writeValueAsBytes(source), true);
        }
        if (source.isObject()) {
            var object = (tools.jackson.databind.node.ObjectNode) target;
            for (var field : source.properties()) {
                object.set(field.getKey(), preserveDecimals(field.getValue(), object.get(field.getKey())));
            }
        } else if (source.isArray()) {
            var array = (tools.jackson.databind.node.ArrayNode) target;
            for (int i = 0; i < source.size(); i++) array.set(i, preserveDecimals(source.get(i), array.get(i)));
        }
        return target;
    }

    private static JsonNode readJackson3(byte[] bytes, boolean decimal) {
        try (var parser = TREE_FACTORY.createParser(bytes)) {
            return decimal ? DECIMAL_READER.readTree(parser) : Jackson2Defaults.mapper().readTree(parser);
        }
    }
}
