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

package com.networknt.kafka.common.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Provides conversion of JSON to/from Avro.
 */
public final class AvroConverter implements SchemaConverter {

  private static final Logger log = LoggerFactory.getLogger(AvroConverter.class);

  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

  /**
   * Converts Avro data (including primitive types) to their equivalent JsonNode representation.
   *
   * @param value the value to convert
   * @return an object containing the root JsonNode representing the converted object and the size
   *     in bytes of the data when serialized
   */
  @Override
  public JsonNodeAndSize toJson(Object value) {
    try {
      byte[] bytes = AvroSchemaUtils.toJson(value);
      if (bytes == null) {
        return new JsonNodeAndSize(NullNode.getInstance(), 0);
      }
      return new JsonNodeAndSize(JSON_MAPPER.readTree(bytes), bytes.length);
    } catch (IOException e) {
      // These can be generated by Avro's JSON encoder, the output stream operations, and the
      // Jackson ObjectMapper.readTree() call.
      log.error("Jackson failed to deserialize JSON generated by Avro's JSON encoder: ", e);
      throw new ConversionException("Failed to convert Avro to JSON: " + e.getMessage());
    } catch (RuntimeException e) {
      // Catch-all since it's possible for, e.g., Avro to throw many different RuntimeExceptions
      log.error("Unexpected exception convertion Avro to JSON: ", e);
      throw new ConversionException("Failed to convert Avro to JSON: " + e.getMessage());
    }
  }
}
