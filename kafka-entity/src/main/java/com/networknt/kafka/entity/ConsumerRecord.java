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

import java.util.Map;

public class ConsumerRecord<K, V> {
  String topic;
  K key;
  V value;
  int partition;
  long offset;
  Map<String, String> headers;

  ConsumerRecord() {
  }

  public ConsumerRecord(String topic, K key, V value, Map<String, String> headers, int partition, long offset) {
    this.topic = topic;
    this.key = key;
    this.value = value;
    this.headers = headers;
    this.partition = partition;
    this.offset = offset;
  }

  public String getTopic() {
    return topic;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  public Map<String, String> getHeaders() { return headers; }

  public int getPartition() {
    return partition;
  }

  public long getOffset() {
    return offset;
  }

  public static <K, V> ConsumerRecord<K, V> create(
      String topic, K key, V value, Map<String, String> headers, int partition, long offset) {
    return new ConsumerRecord<>(topic, key, value, headers, partition, offset);
  }
}
