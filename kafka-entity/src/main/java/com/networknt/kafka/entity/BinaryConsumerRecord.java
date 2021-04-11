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
import com.google.protobuf.ByteString;
import com.networknt.config.JsonMapper;

import java.util.*;

public final class BinaryConsumerRecord {

  private final String topic;

  private final byte[] key;

  private final byte[] value;

  private final Map<String, String> headers;

  private final Integer partition;

  private final Long offset;

  @JsonCreator
  private BinaryConsumerRecord(
      @JsonProperty("topic") String topic,
      @JsonProperty("key") byte[] key,
      @JsonProperty("value") byte[] value,
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
  public String getKey() {
    return key != null ? new String(Base64.getEncoder().encode(key)) : null;
  }

  @JsonProperty
  public String getValue() {
    return value != null ? new String(Base64.getEncoder().encode(value)) : null;
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

  public static BinaryConsumerRecord fromConsumerRecord(
      ConsumerRecord<ByteString, ByteString> record) {
    if (record.getPartition() < 0) {
      throw new IllegalArgumentException();
    }
    if (record.getOffset() < 0) {
      throw new IllegalArgumentException();
    }
    return new BinaryConsumerRecord(
        Objects.requireNonNull(record.getTopic()),
        record.getKey() != null ? record.getKey().toByteArray() : null,
        record.getValue() != null ? record.getValue().toByteArray() : null,
        record.getHeaders(),
        record.getPartition(),
        record.getOffset());
  }

  public ConsumerRecord<ByteString, ByteString> toConsumerRecord() {
    if (topic == null) {
      throw new IllegalStateException();
    }
    if (partition == null || partition < 0) {
      throw new IllegalStateException();
    }
    if (offset == null || offset < 0) {
      throw new IllegalStateException();
    }
    return ConsumerRecord.create(
        topic,
        key != null ? ByteString.copyFrom(key) : null,
        value != null ? ByteString.copyFrom(value) : null,
        headers,
        partition,
        offset);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BinaryConsumerRecord that = (BinaryConsumerRecord) o;
    return Objects.equals(topic, that.topic)
        && Arrays.equals(key, that.key)
        && Arrays.equals(value, that.value)
        && Objects.equals(headers, that.headers)
        && Objects.equals(partition, that.partition)
        && Objects.equals(offset, that.offset);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(topic, headers, partition, offset);
    result = 31 * result + Arrays.hashCode(key);
    result = 31 * result + Arrays.hashCode(value);
    return result;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", BinaryConsumerRecord.class.getSimpleName() + "[", "]")
        .add("topic='" + topic + "'")
        .add("key=" + Arrays.toString(key))
        .add("value=" + Arrays.toString(value))
        .add("headers" + JsonMapper.toJson(headers))
        .add("partition=" + partition)
        .add("offset=" + offset)
        .toString();
  }
}