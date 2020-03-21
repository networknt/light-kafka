package com.networknt.kafka.common;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvroDeserializer extends AbstractAvroDeserializer {
    static final Logger logger = LoggerFactory.getLogger(AvroDeserializer.class);

    public AvroDeserializer() {
        this.useSpecificAvroReader = false;
    }

    public AvroDeserializer(boolean useSpecificAvroReader) {
        this.useSpecificAvroReader = useSpecificAvroReader;
    }

    public Object deserialize(String s, byte[] bytes) {
        return deserialize(bytes);
    }

    /**
     * Pass a reader schema to get an Avro projection
     */
    public Object deserialize(String s, byte[] bytes, Schema readerSchema) {
        return deserialize(bytes, readerSchema);
    }

}
