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
import java.util.Optional;
import com.networknt.kafka.entity.EmbeddedFormat;

/**
 * A facade covering serializers of all supported {@link EmbeddedFormat formats}.
 */
public interface RecordSerializer {

    /**
     * Serializes the given {@code data} into a {@link byte[]}.
     *
     * <p>Returns {@link Optional#empty()} if {@code data} {@link JsonNode#isNull() is null}.
     */
    Optional<ByteString> serialize(
            EmbeddedFormat format,
            String topicName,
            Optional<RegisteredSchema> schema,
            JsonNode data,
            boolean isKey);
}
