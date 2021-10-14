package com.networknt.kafka.common;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS;

public abstract class AbstractAvroSerializer extends AbstractAvroSerDe {
    static final Logger logger = LoggerFactory.getLogger(AbstractAvroSerializer.class);

    private final EncoderFactory encoderFactory = EncoderFactory.get();

    protected byte[] serializeImpl(Object object)  {
        Schema schema = null;
        // null needs to treated specially since the client most likely just wants to send
        // an individual null value instead of making the subject a null type. Also, null in
        // Kafka has a special meaning for deletion in a topic with the compact retention policy.
        // Therefore, we will bypass schema registration and return a null value in Kafka, instead
        // of an Avro encoded null.
        if (object == null) {
            return null;
        }
        String restClientErrorMsg = "";
        try {
            int id;
            schema = AvroSchemaUtils.getSchema(object);
            String subject = schema.getFullName();
            Boolean autoRegistry = (Boolean)config.get("schema.registry.auto.register.schemas");
            if (autoRegistry.booleanValue()) {
                restClientErrorMsg = "Error registering Avro schema: ";
                id = schemaRegistry.register(subject, new AvroSchema(schema));
            } else {
                restClientErrorMsg = "Error retrieving Avro schema: ";
                id = schemaRegistry.getId(subject, new AvroSchema(schema));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(MAGIC_BYTE);
            out.write(ByteBuffer.allocate(idSize).putInt(id).array());
            if (object instanceof byte[]) {
                out.write((byte[]) object);
            } else {
                BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
                DatumWriter<Object> writer;
                Object value = object instanceof NonRecordContainer ? ((NonRecordContainer) object).getValue()
                                : object;
                if (value instanceof SpecificRecord) {
                    writer = new SpecificDatumWriter<>(schema);
                } else {
                    writer = new GenericDatumWriter<>(schema);
                }
                writer.write(value, encoder);
                encoder.flush();
            }
            byte[] bytes = out.toByteArray();
            out.close();
            return bytes;
        } catch (IOException | RuntimeException e) {
            // avro serialization can throw AvroRuntimeException, NullPointerException,
            // ClassCastException, etc
            throw new RuntimeException("Error serializing Avro message", e);
        } catch (RestClientException e) {
            throw new RuntimeException(restClientErrorMsg + schema, e);
        }
    }
}
