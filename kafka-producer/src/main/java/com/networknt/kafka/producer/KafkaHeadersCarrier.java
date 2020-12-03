package com.networknt.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

public class KafkaHeadersCarrier implements io.opentracing.propagation.TextMap {
    private final static Logger logger = LoggerFactory.getLogger(KafkaHeadersCarrier.class);
    private final ProducerRecord record;

    public KafkaHeadersCarrier(ProducerRecord record) {
        this.record = record;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("carrier is write-only");
    }

    @Override
    public void put(String key, String value) {
        if(logger.isDebugEnabled()) logger.debug("key = " + key + " value = " + value);
        record.headers().add(key, value.getBytes(StandardCharsets.UTF_8));
    }
}
