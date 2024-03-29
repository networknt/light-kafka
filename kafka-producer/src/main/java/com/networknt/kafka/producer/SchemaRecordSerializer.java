/*
 * Copyright 2021 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.networknt.kafka.producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.networknt.exception.FrameworkException;
import com.networknt.status.Status;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaUtils;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchemaUtils;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaUtils;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerializer;
import io.confluent.kafka.serializers.json.AbstractKafkaJsonSchemaSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import org.apache.avro.AvroTypeException;
import org.apache.kafka.common.errors.SerializationException;
import org.everit.json.schema.ValidationException;
import com.networknt.kafka.entity.EmbeddedFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class SchemaRecordSerializer {
    private Logger logger = LoggerFactory.getLogger(SchemaRecordSerializer.class);

    private final AvroSerializer avroSerializer;
    private final JsonSchemaSerializer jsonschemaSerializer;
    private final ProtobufSerializer protobufSerializer;
    private final String SERIALIZE_SCHEMA_EXCEPTION = "ERR12206";

    public SchemaRecordSerializer(
            SchemaRegistryClient schemaRegistryClient,
            Map<String, Object> avroSerializerConfigs,
            Map<String, Object> jsonschemaSerializerConfigs,
            Map<String, Object> protobufSerializerConfigs) {
        avroSerializer = new AvroSerializer(schemaRegistryClient, avroSerializerConfigs);
        jsonschemaSerializer = new JsonSchemaSerializer(schemaRegistryClient, jsonschemaSerializerConfigs);
        protobufSerializer = new ProtobufSerializer(schemaRegistryClient, protobufSerializerConfigs);
    }

    public Optional<ByteString> serialize(
            int index,
            EmbeddedFormat format,
            String topicName,
            Optional<RegisteredSchema> schema,
            JsonNode data,
            boolean isKey) {
        if (data.isNull()) {
            return Optional.empty();
        }
        if (!schema.isPresent()) {
            throw new SerializationException(
                    String.format(
                            "Cannot serialize a non-null %s without a %s schema.",
                            isKey ? "key" : "value", isKey ? "key" : "value"));
        }

        switch (format) {
            case AVRO:
                return Optional.of(serializeAvro(index, schema.get().getSubject(), schema.get(), data));

            case JSONSCHEMA:
                return Optional.of(serializeJsonschema(index, schema.get().getSubject(), schema.get(), data));

            case PROTOBUF:
                return Optional.of(
                        serializeProtobuf(index, schema.get().getSubject(), topicName, schema.get(), data, isKey));

            default:
                throw new AssertionError(String.format("Unexpected enum constant: %s", format));
        }
    }

    private ByteString serializeAvro(int index, String subject, RegisteredSchema schema, JsonNode data) {
        AvroSchema avroSchema = (AvroSchema) schema.getSchema();
        Object record;
        try {
            record = AvroSchemaUtils.toObject(data, avroSchema);
        } catch (AvroTypeException | IOException e) {
            logger.error("Exception for data at index: " + index +" with schemaId: " + schema.getSchemaId());
            Status status = new Status(SERIALIZE_SCHEMA_EXCEPTION, "avro , index in batch : "+ index, e.getMessage());
            throw new FrameworkException(status);
        }
        return ByteString.copyFrom(avroSerializer.serialize(subject, avroSchema, record));
    }

    private ByteString serializeJsonschema(int index, String subject, RegisteredSchema schema, JsonNode data) {
        JsonSchema jsonSchema = (JsonSchema) schema.getSchema();
        Object record;
        try {
            record = JsonSchemaUtils.toObject(data, jsonSchema);
        } catch (IOException | ValidationException e) {
            logger.error("Exception for data at index: " + index +" with schemaId: " + schema.getSchemaId());
            Status status = new Status(SERIALIZE_SCHEMA_EXCEPTION, "jsonschema , index in batch : "+ index, e.getMessage());
            throw new FrameworkException(status);
        }
        return ByteString.copyFrom(jsonschemaSerializer.serialize(subject, jsonSchema, record));
    }

    private ByteString serializeProtobuf(
            int index, String subject, String topicName, RegisteredSchema schema, JsonNode data, boolean isKey) {
        ProtobufSchema protobufSchema = (ProtobufSchema) schema.getSchema();
        Message record;
        try {
            record = (Message) ProtobufSchemaUtils.toObject(data, protobufSchema);
        } catch (IOException e) {
            logger.error("Exception for data with schemaId: " + schema.getSchemaId());
            Status status = new Status(SERIALIZE_SCHEMA_EXCEPTION, "protobuf", e.getMessage());
            throw new FrameworkException(status);
        }
        return ByteString.copyFrom(
                protobufSerializer.serialize(subject, topicName, protobufSchema, record, isKey));
    }

    private static final class AvroSerializer extends AbstractKafkaAvroSerializer {

        private AvroSerializer(SchemaRegistryClient schemaRegistryClient, Map<String, Object> configs) {
            this.schemaRegistry = requireNonNull(schemaRegistryClient);
            configure(serializerConfig(configs));
        }

        private byte[] serialize(String subject, AvroSchema schema, Object data) {
            return serializeImpl(subject, data, schema);
        }
    }

    private static final class JsonSchemaSerializer
            extends AbstractKafkaJsonSchemaSerializer<Object> {

        private JsonSchemaSerializer(
                SchemaRegistryClient schemaRegistryClient, Map<String, Object> configs) {
            this.schemaRegistry = requireNonNull(schemaRegistryClient);
            configure(serializerConfig(configs));
        }

        private byte[] serialize(String subject, JsonSchema schema, Object data) {
            return serializeImpl(subject, JsonSchemaUtils.getValue(data), schema);
        }
    }

    private static final class ProtobufSerializer extends KafkaProtobufSerializer<Message> {

        private ProtobufSerializer(
                SchemaRegistryClient schemaRegistryClient, Map<String, Object> configs) {
            this.schemaRegistry = requireNonNull(schemaRegistryClient);
            configure(serializerConfig(configs));
        }

        private byte[] serialize(
                String subject, String topicName, ProtobufSchema schema, Message data, boolean isKey) {
            return serializeImpl(subject, topicName, isKey, data, schema);
        }
    }
}
