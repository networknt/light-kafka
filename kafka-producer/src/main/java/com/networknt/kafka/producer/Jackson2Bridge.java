package com.networknt.kafka.producer;

import com.networknt.config.Jackson2Defaults;
import tools.jackson.databind.JsonNode;

import java.io.IOException;

/** Byte-only conversion for Confluent 8.3.2; Jackson 2 types never enter public producer APIs. */
final class Jackson2Bridge {
    // Input is generated from an existing tree, not read from an external stream.
    // Keep its size acceptance without changing request/config parsing or writer limits.
    private static final com.fasterxml.jackson.databind.ObjectMapper CONFLUENT_MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper(com.fasterxml.jackson.core.JsonFactory.builder()
                    .streamReadConstraints(com.fasterxml.jackson.core.StreamReadConstraints.builder()
                            .maxStringLength(Integer.MAX_VALUE).maxNameLength(Integer.MAX_VALUE)
                            .maxNumberLength(Integer.MAX_VALUE).build()).build());
    // Only explicit decimal nodes use this reader. Ordinary doubles keep their
    // original representation, including signed zero and exponent notation.
    private static final com.fasterxml.jackson.databind.ObjectReader LEGACY_DECIMAL_READER =
            CONFLUENT_MAPPER.readerFor(com.fasterxml.jackson.databind.JsonNode.class)
                    .with(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                    .without(com.fasterxml.jackson.databind.cfg.JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES);

    private Jackson2Bridge() { }

    static com.fasterxml.jackson.databind.JsonNode toJackson2(JsonNode node) throws IOException {
        return preserveDecimals(node, CONFLUENT_MAPPER.readTree(Jackson2Defaults.mapper().writeValueAsBytes(node)));
    }

    private static com.fasterxml.jackson.databind.JsonNode preserveDecimals(
            JsonNode source, com.fasterxml.jackson.databind.JsonNode target) throws IOException {
        if (source.isBigDecimal()) {
            return LEGACY_DECIMAL_READER.readTree(Jackson2Defaults.mapper().writeValueAsBytes(source));
        }
        if (source.isObject()) {
            var object = (com.fasterxml.jackson.databind.node.ObjectNode) target;
            for (var field : source.properties()) {
                var current = object.get(field.getKey());
                var replacement = preserveDecimals(field.getValue(), current);
                if (replacement != current) object.set(field.getKey(), replacement);
            }
        } else if (source.isArray()) {
            var array = (com.fasterxml.jackson.databind.node.ArrayNode) target;
            for (int i = 0; i < source.size(); i++) {
                var current = array.get(i);
                var replacement = preserveDecimals(source.get(i), current);
                if (replacement != current) array.set(i, replacement);
            }
        }
        return target;
    }
}
