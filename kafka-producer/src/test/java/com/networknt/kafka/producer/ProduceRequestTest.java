package com.networknt.kafka.producer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Test the JSON serialization for the object with the Jackson ObjectMapper
 *
 * @author Steve Hu
 */
public class ProduceRequestTest {
    @Test
    public void testToJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        JsonNode jsonNode = mapper.readTree(json);
        ProduceRecord produceRecord = new ProduceRecord();
        produceRecord.setValue(Optional.of(jsonNode));
        List<ProduceRecord> records = new ArrayList<>();
        records.add(produceRecord);
        ProduceRequest produceRequest = new ProduceRequest();
        produceRequest.setRecords(records);
        String result = mapper.writeValueAsString(produceRequest);
        System.out.println(result);

    }

}
