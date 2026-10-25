package com.networknt.kafka.producer;

import com.networknt.config.Jackson2Defaults;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class Jackson2BridgeTest {
    @Test
    void convertsNestedJsonWithoutCrossingPublicApis() throws Exception {
        JsonNode input = Jackson2Defaults.mapper().readTree("""
                {"array":[true,null,1.25,123456789012345678901234567890,{"text":"雪 ☃"}],"empty":{}}
                """);
        assertEquals(input.toString(), Jackson2Bridge.toJackson2(input).toString());
    }

    @Test
    void preservesPreciseDecimalsAndTheirScale() throws Exception {
        BigDecimal value = new BigDecimal("0.1234567890123456789012345678900");
        ObjectNode input = Jackson2Defaults.mapper().createObjectNode();
        input.putArray("array").addObject().put("decimal", value);
        var legacy = Jackson2Bridge.toJackson2(input);
        assertEquals(value, legacy.get("array").get(0).get("decimal").decimalValue());
    }

    @Test
    void preservesOrdinaryFloatingPointWireRepresentation() throws Exception {
        JsonNode input = Jackson2Defaults.mapper().readTree("[1.0,-0.0,1.0E20,1.25]");
        var legacy = Jackson2Bridge.toJackson2(input);
        assertEquals(input.toString(), legacy.toString());
    }

    @Test
    void doesNotAddParserSizeLimitsToAlreadyConstructedTrees() throws Exception {
        var input = Jackson2Defaults.mapper().createObjectNode();
        input.put("large", "x".repeat(20_000_001));
        input.put("number", new java.math.BigInteger("9".repeat(1100)));
        input.put("decimal", new BigDecimal("9".repeat(1100) + ".0010"));
        input.put("n".repeat(50_001), "long name");
        assertEquals(input.toString(), Jackson2Bridge.toJackson2(input).toString());
    }

    @Test
    void convertsRootNullAndScalarValues() throws Exception {
        for (String json : new String[]{"null", "true", "42", "\"雪\"", "[]"}) {
            JsonNode node = Jackson2Defaults.mapper().readTree(json);
            assertEquals(node.toString(), Jackson2Bridge.toJackson2(node).toString());
        }
    }
}
