package com.networknt.kafka.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;
import java.util.Optional;

public class ProduceRecord {
    @JsonProperty("key")
    Optional<JsonNode> key;
    @JsonProperty("value")
    Optional<JsonNode> value;
    @JsonProperty("partition")
    Optional<Integer> partition;
    @JsonProperty("traceabilityId")
    Optional<String> traceabilityId;
    @JsonProperty("correlationId")
    Optional<String> correlationId;

    public ProduceRecord() {
    }

    public ProduceRecord(Optional<Integer> partition, Optional<JsonNode> key, Optional<JsonNode> value, Optional<String> traceabilityId, Optional<String> correlationId) {
        this.key = key;
        this.value = value;
        this.partition = partition;
        this.traceabilityId = traceabilityId;
        this.correlationId = correlationId;
    }

    public static ProduceRecord create(JsonNode key, JsonNode value) {
        return create(/* partition= */ null, key, value, null, null);
    }

    public static ProduceRecord create(
            Integer partition, JsonNode key, JsonNode value, String traceabilityId, String correlationId) {
        return new ProduceRecord(
                Optional.ofNullable(partition), Optional.ofNullable(key), Optional.ofNullable(value), Optional.ofNullable(traceabilityId), Optional.ofNullable(correlationId));
    }

    @JsonCreator
    static ProduceRecord fromJson(
            @JsonProperty("partition") Integer partition,
            @JsonProperty("key") JsonNode key,
            @JsonProperty("value") JsonNode value,
            @JsonProperty("traceabilityId") String traceabilityId,
            @JsonProperty("correlationId") String correlationId) {
        return create(partition, key, value, traceabilityId, correlationId);
    }

    public Optional<JsonNode> getKey() {
        return key;
    }

    public void setKey(Optional<JsonNode> key) {
        this.key = key;
    }

    public Optional<JsonNode> getValue() {
        return value;
    }

    public void setValue(Optional<JsonNode> value) {
        this.value = value;
    }

    public Optional<Integer> getPartition() {
        return partition;
    }

    public void setPartition(Optional<Integer> partition) {
        this.partition = partition;
    }

    public Optional<String> getTraceabilityId() {
        return traceabilityId;
    }

    public void setTraceabilityId(Optional<String> traceabilityId) {
        this.traceabilityId = traceabilityId;
    }

    public Optional<String> getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(Optional<String> correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProduceRecord that = (ProduceRecord) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value) && Objects.equals(partition, that.partition) && Objects.equals(traceabilityId, that.traceabilityId) && Objects.equals(correlationId, that.correlationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, partition, traceabilityId, correlationId);
    }
}