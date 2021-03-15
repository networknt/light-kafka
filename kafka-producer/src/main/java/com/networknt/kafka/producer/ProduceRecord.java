package com.networknt.kafka.producer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class ProduceRecord {
    @JsonProperty("key")
    Optional<JsonNode> key;
    @JsonProperty("value")
    Optional<JsonNode> value;
    @JsonProperty("partition")
    Optional<Integer> partition;

    public ProduceRecord() {
    }

    public ProduceRecord(Optional<Integer> partition, Optional<JsonNode> key, Optional<JsonNode> value) {
        this.key = key;
        this.value = value;
        this.partition = partition;
    }

    public static ProduceRecord create(@Nullable JsonNode key, @Nullable JsonNode value) {
        return create(/* partition= */ null, key, value);
    }

    public static ProduceRecord create(
            @Nullable Integer partition, @Nullable JsonNode key, @Nullable JsonNode value) {
        return new ProduceRecord(
                Optional.ofNullable(partition), Optional.ofNullable(key), Optional.ofNullable(value));
    }

    @JsonCreator
    static ProduceRecord fromJson(
            @JsonProperty("partition") @Nullable Integer partition,
            @JsonProperty("key") @Nullable JsonNode key,
            @JsonProperty("value") @Nullable JsonNode value) {
        return create(partition, key, value);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProduceRecord that = (ProduceRecord) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value) && Objects.equals(partition, that.partition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, partition);
    }
}
