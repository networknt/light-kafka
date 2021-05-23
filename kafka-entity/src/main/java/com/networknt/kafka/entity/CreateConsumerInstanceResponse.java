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

public final class CreateConsumerInstanceResponse {

  private final String instanceId;

  private final String baseUri;

  public CreateConsumerInstanceResponse(
      @JsonProperty("instance_id") String instanceId,
      @JsonProperty("base_uri") String baseUri
  ) {
    this.instanceId = instanceId;
    this.baseUri = baseUri;
  }

  @JsonProperty("instance_id")
  public String getInstanceId() {
    return instanceId;
  }

  @JsonProperty("base_uri")
  public String getBaseUri() {
    return baseUri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateConsumerInstanceResponse that = (CreateConsumerInstanceResponse) o;
    return Objects.equals(instanceId, that.instanceId) && Objects.equals(baseUri, that.baseUri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(instanceId, baseUri);
  }

  @Override
  public String toString() {
    return new StringJoiner(
        ", ", CreateConsumerInstanceResponse.class.getSimpleName() + "[", "]")
        .add("instanceId='" + instanceId + "'")
        .add("baseUri='" + baseUri + "'")
        .toString();
  }
}
