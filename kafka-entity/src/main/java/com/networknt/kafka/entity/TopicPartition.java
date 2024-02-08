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

public final class TopicPartition {

  private final String topic;

  private final Integer partition;

  public TopicPartition(
      @JsonProperty("topic") String topic,
      @JsonProperty("partition") Integer partition
  ) {
    this.topic = topic;
    this.partition = partition;
  }

  @JsonProperty
  public String getTopic() {
    return topic;
  }

  @JsonProperty
  public Integer getPartition() {
    return partition;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TopicPartition that = (TopicPartition) o;
    return Objects.equals(topic, that.topic) && Objects.equals(partition, that.partition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(topic, partition);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TopicPartition.class.getSimpleName() + "[", "]")
        .add("topic='" + topic + "'")
        .add("partition=" + partition)
        .toString();
  }
}
