package com.networknt.kafka.common;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.errors.SerializationException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class AbstractAvroDeserializer extends AbstractAvroSerDe {
    private final DecoderFactory decoderFactory = DecoderFactory.get();
    protected boolean useSpecificAvroReader = false;
    private final Map<String, Schema> readerSchemaCache = new ConcurrentHashMap<>();

    private ByteBuffer getByteBuffer(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        if (buffer.get() != MAGIC_BYTE) {
            throw new RuntimeException("Unknown magic byte!");
        }
        return buffer;
    }

    /**
     * Deserializes the payload without including schema information for primitive types, maps, and
     * arrays. Just the resulting deserialized object is returned.
     *
     * <p>This behavior is the norm for Decoders/Deserializers.
     *
     * @param payload serialized data
     * @return the deserialized object
     */
    public Object deserialize(byte[] payload) throws SerializationException {
        return deserialize(false, null, null, payload, null);
    }

    /**
     * Just like single-parameter version but accepts an Avro schema to use for reading
     *
     * @param payload      serialized data
     * @param readerSchema schema to use for Avro read (optional, enables Avro projection)
     * @return the deserialized object
     */
    public Object deserialize(byte[] payload, Schema readerSchema) throws SerializationException {
        return deserialize(false, null, null, payload, readerSchema);
    }

    // The Object return type is a bit messy, but this is the simplest way to have
    // flexible decoding and not duplicate deserialization code multiple times for different variants.
    protected Object deserialize(boolean includeSchemaAndVersion, String topic, Boolean isKey,
                                 byte[] payload, Schema readerSchema) throws SerializationException {
        // Even if the caller requests schema & version, if the payload is null we cannot include it.
        // The caller must handle this case.
        if (payload == null) {
            return null;
        }

        int id = -1;
        try {
            ByteBuffer buffer = getByteBuffer(payload);
            id = buffer.getInt();
            Schema schema = schemaRegistry.getById(id);

            int length = buffer.limit() - 1 - idSize;
            final Object result;
            if (schema.getType().equals(Schema.Type.BYTES)) {
                byte[] bytes = new byte[length];
                buffer.get(bytes, 0, length);
                result = bytes;
            } else {
                int start = buffer.position() + buffer.arrayOffset();
                DatumReader reader = getDatumReader(schema, readerSchema);
                Object
                        object =
                        reader.read(null, decoderFactory.binaryDecoder(buffer.array(), start, length, null));

                if (schema.getType().equals(Schema.Type.STRING)) {
                    object = object.toString(); // Utf8 -> String
                }
                result = object;
            }
            return result;

        } catch (IOException | RuntimeException e) {
            // avro deserialization may throw AvroRuntimeException, NullPointerException, etc
            throw new SerializationException("Error deserializing Avro message for id " + id, e);
        } catch (RestClientException e) {
            throw new SerializationException("Error retrieving Avro schema for id " + id, e);
        }
    }

    private DatumReader getDatumReader(Schema writerSchema, Schema readerSchema) {
        boolean writerSchemaIsPrimitive =
                AvroSchemaUtils.getPrimitiveSchemas().values().contains(writerSchema);
        // do not use SpecificDatumReader if writerSchema is a primitive
        if (useSpecificAvroReader && !writerSchemaIsPrimitive) {
            if (readerSchema == null) {
                readerSchema = getReaderSchema(writerSchema);
            }
            return new SpecificDatumReader(writerSchema, readerSchema);
        } else {
            if (readerSchema == null) {
                return new GenericDatumReader(writerSchema);
            }
            return new GenericDatumReader(writerSchema, readerSchema);
        }
    }

    @SuppressWarnings("unchecked")
    private Schema getReaderSchema(Schema writerSchema) {
        Schema readerSchema = readerSchemaCache.get(writerSchema.getFullName());
        if (readerSchema == null) {
            Class<SpecificRecord> readerClass = SpecificData.get().getClass(writerSchema);
            if (readerClass != null) {
                try {
                    readerSchema = readerClass.newInstance().getSchema();
                } catch (InstantiationException e) {
                    throw new SerializationException(writerSchema.getFullName()
                            + " specified by the "
                            + "writers schema could not be instantiated to "
                            + "find the readers schema.");
                } catch (IllegalAccessException e) {
                    throw new SerializationException(writerSchema.getFullName()
                            + " specified by the "
                            + "writers schema is not allowed to be instantiated "
                            + "to find the readers schema.");
                }
                readerSchemaCache.put(writerSchema.getFullName(), readerSchema);
            } else {
                throw new SerializationException("Could not find class "
                        + writerSchema.getFullName()
                        + " specified in writer's schema whilst finding reader's "
                        + "schema for a SpecificRecord.");
            }
        }
        return readerSchema;
    }
}
