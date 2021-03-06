/*
 * Copyright 2018 Confluent Inc.
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

package com.networknt.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.kafka.common.KafkaConsumerConfig;
import com.networknt.kafka.common.converter.SchemaConverter;
import com.networknt.kafka.entity.ConsumerInstanceConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Schema-specific implementation of KafkaConsumerState, which decodes
 * into Objects or primitive types.
 */
public final class SchemaKafkaConsumerState
    extends KafkaConsumerState<Object, Object, JsonNode, JsonNode> {

  private final SchemaConverter schemaConverter;

  public SchemaKafkaConsumerState(
      KafkaConsumerConfig config,
      ConsumerInstanceConfig consumerInstanceConfig,
      ConsumerInstanceId instanceId,
      Consumer consumer,
      SchemaConverter schemaConverter) {
    super(config, consumerInstanceConfig, instanceId, consumer);
    this.schemaConverter = schemaConverter;
  }

  @Override
  public ConsumerRecordAndSize<JsonNode, JsonNode> createConsumerRecord(
      ConsumerRecord<Object, Object> record) {
    SchemaConverter.JsonNodeAndSize keyNode = schemaConverter.toJson(record.key());
    SchemaConverter.JsonNodeAndSize valueNode = schemaConverter.toJson(record.value());
    return new ConsumerRecordAndSize<>(
        com.networknt.kafka.entity.ConsumerRecord.create(
            record.topic(),
            keyNode.getJson(),
            valueNode.getJson(),
            record.headers() != null ? convertHeaders(record.headers()) : null,
            record.partition(),
            record.offset()),
        keyNode.getSize() + valueNode.getSize());
  }
}
