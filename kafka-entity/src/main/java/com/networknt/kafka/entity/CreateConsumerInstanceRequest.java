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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public final class CreateConsumerInstanceRequest {

  private static final EmbeddedFormat DEFAULT_KEY_FORMAT = EmbeddedFormat.STRING;
  private static final EmbeddedFormat DEFAULT_VALUE_FORMAT = EmbeddedFormat.BINARY;

  public static final CreateConsumerInstanceRequest PROTOTYPE =
      new CreateConsumerInstanceRequest(
          /* id= */ null,
          /* name= */ null,
          /* keyFormat= */ null,
          /* valueFormat = */ null,
          /* autoOffsetReset= */ null,
          /* autoCommitEnable= */ null,
          /* responseMinBytes= */ null,
          /* requestWaitMs= */ null);

  private final String id;

  private final String name;

  private final EmbeddedFormat keyFormat;

  private final EmbeddedFormat valueFormat;

  private final String autoOffsetReset;

  private final String autoCommitEnable;

  private final Integer responseMinBytes;

  private final Integer requestWaitMs;

  @JsonCreator
  public CreateConsumerInstanceRequest(
      @JsonProperty("id") String id,
      @JsonProperty("name") String name,
      @JsonProperty("keyFormat") String keyFormat,
      @JsonProperty("valueFormat") String valueFormat,
      @JsonProperty("auto.offset.reset") @JsonAlias("autoOffsetReset") String autoOffsetReset,
      @JsonProperty("auto.commit.enable") @JsonAlias("autoCommitEnable") String autoCommitEnable,
      @JsonProperty("fetch.min.bytes") @JsonAlias("responseMinBytes") Integer
          responseMinBytes,
      @JsonProperty("consumer.request.timeout.ms") @JsonAlias("requestWaitMs") Integer
          requestWaitMs
  ) {
    this.id = id;
    this.name = name;
    this.keyFormat = computeFormat(keyFormat, true);
    this.valueFormat = computeFormat(valueFormat, false);
    this.autoOffsetReset = autoOffsetReset;
    this.autoCommitEnable = autoCommitEnable;
    this.responseMinBytes = responseMinBytes;
    this.requestWaitMs = requestWaitMs;
  }

  private static EmbeddedFormat computeFormat(String format, boolean isKey) {
    if (format == null) {
      if(isKey) {
        return DEFAULT_KEY_FORMAT;
      } else {
        return DEFAULT_VALUE_FORMAT;
      }
    }
    String formatCanonical = format.toUpperCase();
    for (EmbeddedFormat f : EmbeddedFormat.values()) {
      if (f.name().equals(formatCanonical)) {
        return f;
      }
    }
    throw new RuntimeException("Invalid format type " + format);
  }

  @JsonProperty
  public String getId() {
    return id;
  }

  @JsonProperty
  public String getName() {
    return name;
  }

  @JsonProperty
  public String getKeyFormat() {
    return keyFormat.name().toLowerCase();
  }

  @JsonProperty
  public String getValueFormat() {
    return valueFormat.name().toLowerCase();
  }

  @JsonProperty("auto.offset.reset")
  public String getAutoOffsetReset() {
    return autoOffsetReset;
  }

  @JsonProperty("auto.commit.enable")
  public String getAutoCommitEnable() {
    return autoCommitEnable;
  }

  @JsonProperty("fetch.min.bytes")
  public Integer getResponseMinBytes() {
    return responseMinBytes;
  }

  @JsonProperty("consumer.request.timeout.ms")
  public Integer getRequestWaitMs() {
    return requestWaitMs;
  }

  public ConsumerInstanceConfig toConsumerInstanceConfig() {
    return ConsumerInstanceConfig.create(
        id,
        name,
        keyFormat,
        valueFormat,
        autoOffsetReset,
        autoCommitEnable,
        responseMinBytes,
        requestWaitMs);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateConsumerInstanceRequest that = (CreateConsumerInstanceRequest) o;
    return Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && keyFormat == that.keyFormat
        && valueFormat == that.valueFormat
        && Objects.equals(autoOffsetReset, that.autoOffsetReset)
        && Objects.equals(autoCommitEnable, that.autoCommitEnable)
        && Objects.equals(responseMinBytes, that.responseMinBytes)
        && Objects.equals(requestWaitMs, that.requestWaitMs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, name, keyFormat, valueFormat, autoOffsetReset, autoCommitEnable, responseMinBytes, requestWaitMs);
  }

  @Override
  public String toString() {
    return new StringJoiner(
        ", ", CreateConsumerInstanceRequest.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("name='" + name + "'")
        .add("keyFormat=" + keyFormat)
        .add("valueFormat=" + valueFormat)
        .add("autoOffsetReset='" + autoOffsetReset + "'")
        .add("autoCommitEnable='" + autoCommitEnable + "'")
        .add("responseMinBytes=" + responseMinBytes)
        .add("requestWaitMs=" + requestWaitMs)
        .toString();
  }
}
