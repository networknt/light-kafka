package com.networknt.kafka.common;

public class AvroSerializer extends AbstractAvroSerializer {

    public AvroSerializer() {
    }

    public byte[] serialize(Object record) {
        return serializeImpl(record);
    }

}
