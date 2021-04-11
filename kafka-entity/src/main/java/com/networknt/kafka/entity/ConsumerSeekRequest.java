/*
 * Copyright 2020 Confluent Inc.
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

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ConsumerSeekRequest {
  List<PartitionOffset> offsets;
  List<PartitionTimestamp> timestamps;

  ConsumerSeekRequest() {
  }

  public ConsumerSeekRequest(List<PartitionOffset> offsets, List<PartitionTimestamp> timestamps) {
    this.offsets = offsets;
    this.timestamps = timestamps;
  }

  public void setOffsets(List<PartitionOffset> offsets) {
    this.offsets = offsets;
  }

  public void setTimestamps(List<PartitionTimestamp> timestamps) {
    this.timestamps = timestamps;
  }

  @JsonProperty("offsets")
  public List<PartitionOffset> getOffsets() {
    return offsets;
  }

  @JsonProperty("timestamps")
  public List<PartitionTimestamp> getTimestamps() {
    return timestamps;
  }

  @JsonCreator
  static ConsumerSeekRequest fromJson(
      @JsonProperty("offsets") @Nullable List<PartitionOffset> offsets,
      @JsonProperty("timestamps") @Nullable List<PartitionTimestamp> timestamps
  ) {
    return new ConsumerSeekRequest(offsets, timestamps);
  }

  public static class PartitionOffset {

    String topic;
    int partition;
    long offset;
    String metadata;

    public PartitionOffset() {
    }

    public PartitionOffset(String topic, int partition, long offset, String metadata) {
      this.topic = topic;
      this.partition = partition;
      this.offset = offset;
      this.metadata = metadata;
    }

    @JsonProperty("topic")
    public String getTopic() {
      return topic;
    }

    @JsonProperty("partition")
    public int getPartition() {
      return partition;
    }

    @JsonProperty("offset")
    public long getOffset() {
      return offset;
    }

    @JsonProperty("metadata")
    public String getMetadata() {
      return metadata;
    }

    public void setTopic(String topic) {
      this.topic = topic;
    }

    public void setPartition(int partition) {
      this.partition = partition;
    }

    public void setOffset(long offset) {
      this.offset = offset;
    }

    public void setMetadata(String metadata) {
      this.metadata = metadata;
    }

    @JsonCreator
    PartitionOffset fromJson(
        @JsonProperty("topic") String topic,
        @JsonProperty("partition") int partition,
        @JsonProperty("offset") long offset,
        @JsonProperty("metadata") @Nullable String metadata
    ) {
      return new PartitionOffset(topic, partition, offset, metadata);
    }
  }

  public class PartitionTimestamp {
    String topic;
    int partition;
    Instant timestamp;
    String metadata;

    PartitionTimestamp() {
    }

    public PartitionTimestamp(String topic, int partition, Instant timestamp, String metadata) {
      this.topic = topic;
      this.partition = partition;
      this.timestamp = timestamp;
      this.metadata = metadata;
    }

    @JsonProperty("topic")
    public String getTopic() {
      return topic;
    }

    @JsonProperty("partition")
    public int getPartition() {
      return partition;
    }

    @JsonProperty("timestamp")
    public Instant getTimestamp() {
      return timestamp;
    }

    @JsonProperty("metadata")
    public String getMetadata() {
      return metadata;
    }

    @JsonCreator
    PartitionTimestamp fromJson(
        @JsonProperty("topic") String topic,
        @JsonProperty("partition") int partition,
        @JsonProperty("timestamp") Instant timestamp,
        @JsonProperty("metadata") @Nullable String metadata
    ) {
      return new PartitionTimestamp(topic, partition, timestamp, metadata);
    }
  }
}
