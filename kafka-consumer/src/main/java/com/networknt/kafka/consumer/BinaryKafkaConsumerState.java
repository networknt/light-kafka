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

import com.google.protobuf.ByteString;
import com.networknt.kafka.common.KafkaConsumerConfig;
import com.networknt.kafka.entity.ConsumerInstanceConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;


/**
 * Binary implementation of KafkaConsumerState that does no decoding, returning the raw bytes
 * directly.
 */
public class BinaryKafkaConsumerState
    extends KafkaConsumerState<byte[], byte[], ByteString, ByteString> {

  public BinaryKafkaConsumerState(
      KafkaConsumerConfig config,
      ConsumerInstanceConfig consumerInstanceConfig,
      ConsumerInstanceId instanceId,
      Consumer consumer
  ) {
    super(config, consumerInstanceConfig, instanceId, consumer);
  }

  @Override
  public ConsumerRecordAndSize<ByteString, ByteString> createConsumerRecord(
      ConsumerRecord<byte[], byte[]> record) {
    long approxSize = (record.key() != null ? record.key().length : 0)
        + (record.value() != null ? record.value().length : 0);

    return new ConsumerRecordAndSize<>(
        com.networknt.kafka.entity.ConsumerRecord.create(
            record.topic(),
            record.key() != null ? ByteString.copyFrom(record.key()) : null,
            record.value() != null ? ByteString.copyFrom(record.value()) : null,
            record.headers() != null ? convertHeaders(record.headers()) : null,
            record.partition(),
            record.offset()),
        approxSize);
  }
}
