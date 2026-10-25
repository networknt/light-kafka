package com.networknt.kafka.producer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.networknt.config.Jackson2Defaults;
import com.networknt.kafka.entity.ProduceRecord;
import com.networknt.kafka.entity.ProduceRequest;
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
    public void testToJson() throws JacksonException {
        ObjectMapper mapper = Jackson2Defaults.mapper();
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
