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

public class ConsumerInstanceConfig {

  String id;
  String name;
  EmbeddedFormat keyFormat;
  EmbeddedFormat valueFormat;
  String autoOffsetReset;
  String autoCommitEnable;
  Integer responseMinBytes;
  Integer requestWaitMs;

  ConsumerInstanceConfig() {
  }

  public ConsumerInstanceConfig(String id, String name, EmbeddedFormat keyFormat, EmbeddedFormat valueFormat, String autoOffsetReset, String autoCommitEnable, Integer responseMinBytes, Integer requestWaitMs) {
    this.id = id;
    this.name = name;
    this.keyFormat = keyFormat;
    this.valueFormat = valueFormat;
    this.autoOffsetReset = autoOffsetReset;
    this.autoCommitEnable = autoCommitEnable;
    this.responseMinBytes = responseMinBytes;
    this.requestWaitMs = requestWaitMs;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public EmbeddedFormat getKeyFormat() {
    return keyFormat;
  }

  public EmbeddedFormat getValueFormat() {
    return valueFormat;
  }

  public String getAutoOffsetReset() {
    return autoOffsetReset;
  }

  public String getAutoCommitEnable() {
    return autoCommitEnable;
  }

  public Integer getResponseMinBytes() {
    return responseMinBytes;
  }

  public Integer getRequestWaitMs() {
    return requestWaitMs;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setKeyFormat(EmbeddedFormat keyFormat) {
    this.keyFormat = keyFormat;
  }

  public void setValueFormat(EmbeddedFormat valueFormat) {
    this.valueFormat = valueFormat;
  }

  public void setAutoOffsetReset(String autoOffsetReset) {
    this.autoOffsetReset = autoOffsetReset;
  }

  public void setAutoCommitEnable(String autoCommitEnable) {
    this.autoCommitEnable = autoCommitEnable;
  }

  public void setResponseMinBytes(Integer responseMinBytes) {
    this.responseMinBytes = responseMinBytes;
  }

  public void setRequestWaitMs(Integer requestWaitMs) {
    this.requestWaitMs = requestWaitMs;
  }

  public static ConsumerInstanceConfig create(EmbeddedFormat keyFormat, EmbeddedFormat valueFormat) {
    return create(
        /* id= */ null,
        /* name= */ null,
        keyFormat,
        valueFormat,
        /* autoOffsetReset= */ null,
        /* autoCommitEnable= */ null,
        /* responseMinBytes= */ null,
        /* requestWaitMs= */ null);
  }

  public static ConsumerInstanceConfig create(
      String id,
      String name,
      EmbeddedFormat keyFormat,
      EmbeddedFormat valueFormat,
      String autoOffsetReset,
      String autoCommitEnable,
      Integer responseMinBytes,
      Integer requestWaitMs
  ) {
    return new ConsumerInstanceConfig(
        id, name, keyFormat, valueFormat, autoOffsetReset, autoCommitEnable, responseMinBytes, requestWaitMs);
  }
}
