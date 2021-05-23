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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public final class TopicPartitionOffsetMetadata {

  private final String topic;

  private final Integer partition;

  private final Long offset;

  private final String metadata;

  public TopicPartitionOffsetMetadata(
      @JsonProperty("topic") String topic,
      @JsonProperty("partition") Integer partition,
      @JsonProperty("offset") Long offset,
      @JsonProperty("metadata") String metadata
  ) {
    this.topic = topic;
    this.partition = partition;
    this.offset = offset;
    this.metadata = metadata;
  }

  @JsonProperty
  public String getTopic() {
    return topic;
  }

  @JsonProperty
  public Integer getPartition() {
    return partition;
  }

  @JsonProperty
  public Long getOffset() {
    return offset;
  }

  @JsonProperty
  public String getMetadata() {
    return metadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TopicPartitionOffsetMetadata that = (TopicPartitionOffsetMetadata) o;
    return Objects.equals(topic, that.topic)
        && Objects.equals(partition, that.partition)
        && Objects.equals(offset, that.offset)
        && Objects.equals(metadata, that.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(topic, partition, offset, metadata);
  }

  @Override
  public String toString() {
    return new StringJoiner(
        ", ", TopicPartitionOffsetMetadata.class.getSimpleName() + "[", "]")
        .add("topic='" + topic + "'")
        .add("partition=" + partition)
        .add("offset=" + offset)
        .add("metadata='" + metadata + "'")
        .toString();
  }
}
