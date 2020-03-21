package com.networknt.kafka.common;

import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AvroConverter {
    public static String toJson(SpecificRecord record, boolean pretty) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            JsonEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(record.getSchema(), baos, pretty);
            SpecificDatumWriter avroWriter = new SpecificDatumWriter(record.getSchema());
            avroWriter.write(record, jsonEncoder);
            jsonEncoder.flush();
            baos.flush();
            byte[] bytes = baos.toByteArray();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
