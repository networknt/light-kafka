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

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.confluent.kafka.serializers.KafkaJsonSerializerConfig;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.networknt.kafka.entity.EmbeddedFormat;
import org.apache.kafka.common.errors.SerializationException;

public class NoSchemaRecordSerializer {

    private final JsonSerializer jsonSerializer;

    public NoSchemaRecordSerializer(Map<String, Object> jsonSerializerConfigs) {
        jsonSerializer = new JsonSerializer(jsonSerializerConfigs);
    }

    public Optional<ByteString> serialize(int index, EmbeddedFormat format, JsonNode data) {
        checkArgument(!format.requiresSchema());

        if (data.isNull()) {
            return Optional.empty();
        }

        switch (format) {
            case BINARY:
                return Optional.of(serializeBinary(index, data));

            case JSON:
                return Optional.of(serializeJson(index, data));

            case STRING:
                return Optional.of(serializeString(index, data));
            default:
                throw new AssertionError(String.format("Unexpected enum constant: %s", format));
        }
    }

    private static ByteString serializeBinary(int index, JsonNode data) {
        if (!data.isTextual()) {
            throw new RuntimeException(String.format("data = %s is not a base64 string at index = %d.", data, index));
        }
        byte[] serialized;
        try {
            serialized = BaseEncoding.base64().decode(data.asText());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    String.format("data = %s is not a valid base64 string at index = %d.", data, index), e);
        }
        return ByteString.copyFrom(serialized);
    }

    private static ByteString serializeString(int index, JsonNode data) {
        if (!data.isTextual()) {
            throw new RuntimeException(String.format("data = %s is not a string at index = %d.", data, index));
        }
        byte[] serialized;
        try {
            serialized = data.asText().getBytes(StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    String.format("data = %s is not a valid string at index = %d.", data, index), e);
        }
        return ByteString.copyFrom(serialized);
    }

    private ByteString serializeJson(int index, JsonNode data) {
        try {
            return ByteString.copyFrom(jsonSerializer.serialize(data));
        }
        catch(Exception e){
            throw new RuntimeException(
                    String.format("data = %s is not a valid json at index = %d.", data, index), e);
        }
    }

    private static final class JsonSerializer extends KafkaJsonSerializer<JsonNode> {

        private JsonSerializer(Map<String, Object> configs) {
            configure(new KafkaJsonSerializerConfig(configs));
        }

        private byte[] serialize(JsonNode data) {
            return serialize(/* topic= */ "", data);
        }
    }
}
