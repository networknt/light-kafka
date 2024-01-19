package com.networknt.kafka.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    String produceString= "{\"partition\": 1,\"key\": \"abc\", \"value\": {\"def\": 123},\"traceabilityId\": \"trace123\",\"correlationId\": \"corr123\",\"headers\": [{\"timestamp\": 123456899}],\"timestamp\": 1234567899}";

    @BeforeEach
    public void initialize() throws JsonProcessingException {
        mapper = new ObjectMapper();
        Map headers = new HashMap();
        headers.put("timestamp", Instant.now().toString());
        List<Map<String,Object>> headersList= new ArrayList();
        headersList.add(headers);
        this.produceRecord = new ProduceRecord(Optional.of(1),Optional.of(mapper.readTree(mapper.writeValueAsString("abc"))),
                Optional.of(mapper.readTree(mapper.writeValueAsString("123"))), Optional.of("traceabilityId"), Optional.of("correlationId"), Optional.of(headersList), Optional.of(Instant.now().getEpochSecond()));
        this.fromJsonRead5Params= ProduceRecord.fromJson(1, mapper.readTree(mapper.writeValueAsString("abc")), mapper.readTree("{\"number\":123}"), "trace123","corr123", null, null);
        this.fromJsonRead7Params= ProduceRecord.fromJson(1, mapper.readTree(mapper.writeValueAsString("abc")), mapper.readTree("{\"number\":123}"), "trace123","corr123", headersList, Instant.now().getEpochSecond());
        fromJsonObject = mapper.readValue(produceString, ProduceRecord.class);
    }

    @Test
    public void serializationTest() throws JsonProcessingException {
        Assertions.assertNotNull(produceRecord);
        Assertions.assertNotNull(fromJsonRead5Params);
        Assertions.assertNotNull(fromJsonRead7Params);
        Assertions.assertNotNull(fromJsonObject);

    }
}
