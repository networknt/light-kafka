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

import javax.annotation.Nullable;

public class ConsumerRecord<K, V> {
  String topic;
  K key;
  V value;
  int partition;
  long offset;

  ConsumerRecord() {
  }

  public ConsumerRecord(String topic, K key, V value, int partition, long offset) {
    this.topic = topic;
    this.key = key;
    this.value = value;
    this.partition = partition;
    this.offset = offset;
  }

  public String getTopic() {
    return topic;
  }

  @Nullable
  public K getKey() {
    return key;
  }

  @Nullable
  public V getValue() {
    return value;
  }

  public int getPartition() {
    return partition;
  }

  public long getOffset() {
    return offset;
  }

  public static <K, V> ConsumerRecord<K, V> create(
      String topic, @Nullable K key, @Nullable V value, int partition, long offset) {
    return new ConsumerRecord<>(topic, key, value, partition, offset);
  }
}
