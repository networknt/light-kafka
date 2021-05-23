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

package com.networknt.kafka.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.config.JsonMapper;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public final class SchemaConsumerRecord {

  private final String topic;

  private final JsonNode key;

  private final JsonNode value;

  private final Map<String, String> headers;

  private final Integer partition;

  private final Long offset;

  @JsonCreator
  private SchemaConsumerRecord(
      @JsonProperty("topic") String topic,
      @JsonProperty("key") JsonNode key,
      @JsonProperty("value") JsonNode value,
      @JsonProperty("headers") Map<String, String> headers,
      @JsonProperty("partition") Integer partition,
      @JsonProperty("offset") Long offset) {
    this.topic = topic;
    this.key = key;
    this.value = value;
    this.headers = headers;
    this.partition = partition;
    this.offset = offset;
  }

  @JsonProperty
  public String getTopic() {
    return topic;
  }

  @JsonProperty
  public JsonNode getKey() {
    return key;
  }

  @JsonProperty
  public JsonNode getValue() {
    return value;
  }

  @JsonProperty
  public Map<String, String> getHeaders() { return headers; }

  @JsonProperty
  public Integer getPartition() {
    return partition;
  }

  @JsonProperty
  public Long getOffset() {
    return offset;
  }

  public static SchemaConsumerRecord fromConsumerRecord(ConsumerRecord<JsonNode, JsonNode> record) {
    if (record.getPartition() < 0) {
      throw new IllegalArgumentException();
    }
    if (record.getOffset() < 0) {
      throw new IllegalArgumentException();
    }
    return new SchemaConsumerRecord(
        Objects.requireNonNull(record.getTopic()),
        record.getKey(),
        record.getValue(),
        record.getHeaders(),
        record.getPartition(),
        record.getOffset());
  }

  public ConsumerRecord<JsonNode, JsonNode> toConsumerRecord() {
    if (topic == null) {
      throw new IllegalStateException();
    }
    if (partition == null || partition < 0) {
      throw new IllegalStateException();
    }
    if (offset == null || offset < 0) {
      throw new IllegalStateException();
    }
    return ConsumerRecord.create(topic, key, value, headers, partition, offset);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SchemaConsumerRecord that = (SchemaConsumerRecord) o;
    return Objects.equals(topic, that.topic)
        && Objects.equals(key, that.key)
        && Objects.equals(value, that.value)
        && Objects.equals(headers, that.headers)
        && Objects.equals(partition, that.partition)
        && Objects.equals(offset, that.offset);
  }

  @Override
  public int hashCode() {
    return Objects.hash(topic, key, value, headers, partition, offset);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SchemaConsumerRecord.class.getSimpleName() + "[", "]")
        .add("topic='" + topic + "'")
        .add("key=" + key)
        .add("value=" + value)
        .add("headers" + JsonMapper.toJson(headers))
        .add("partition=" + partition)
        .add("offset=" + offset)
        .toString();
  }
}
