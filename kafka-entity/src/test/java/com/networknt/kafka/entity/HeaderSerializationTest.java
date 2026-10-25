package com.networknt.kafka.entity;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.networknt.config.Jackson2Defaults;
import kotlin.jvm.internal.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class HeaderSerializationTest {

    ProduceRecord produceRecord;
    ObjectMapper mapper;

    ProduceRecord fromJsonRead5Params;
    ProduceRecord fromJsonRead7Params;

    ProduceRecord fromJsonObject;

    String produceString= "{\"partition\": 1,\"key\": \"abc\", \"value\": {\"def\": 123},\"traceabilityId\": \"trace123\",\"correlationId\": \"corr123\",\"headers\": {\"timestamp\": 123456899},\"timestamp\": 1234567899}";

    @BeforeEach
    public void initialize() throws JacksonException {
        mapper = Jackson2Defaults.mapper();
        Map headers = new HashMap();
        headers.put("timestamp", Instant.now().toString());
        this.produceRecord = new ProduceRecord(Optional.of(1),Optional.of(mapper.readTree(mapper.writeValueAsString("abc"))),
                Optional.of(mapper.readTree(mapper.writeValueAsString("123"))), Optional.of("traceabilityId"), Optional.of("correlationId"), Optional.of(headers), Optional.of(Instant.now().getEpochSecond()));
        this.fromJsonRead5Params= ProduceRecord.fromJson(1, mapper.readTree(mapper.writeValueAsString("abc")), mapper.readTree("{\"number\":123}"), "trace123","corr123", null, null);
        this.fromJsonRead7Params= ProduceRecord.fromJson(1, mapper.readTree(mapper.writeValueAsString("abc")), mapper.readTree("{\"number\":123}"), "trace123","corr123", headers, Instant.now().getEpochSecond());
        fromJsonObject = mapper.readValue(produceString, ProduceRecord.class);
    }

    @Test
    public void serializationTest() throws JacksonException {
        Assertions.assertNotNull(produceRecord);
        Assertions.assertNotNull(fromJsonRead5Params);
        Assertions.assertNotNull(fromJsonRead7Params);
        Assertions.assertNotNull(fromJsonObject);

    }
}
