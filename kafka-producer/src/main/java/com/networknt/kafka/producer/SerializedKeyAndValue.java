package com.networknt.kafka.producer;

import com.google.protobuf.ByteString;

import java.util.Objects;
import java.util.Optional;

public class SerializedKeyAndValue {
    Optional<Integer> partitionId;
    Optional<String> traceabilityId;
    Optional<String> correlationId;
    Optional<ByteString> key;
    Optional<ByteString> value;

    public SerializedKeyAndValue(Optional<Integer> partitionId, Optional<String> traceabilityId, Optional<String> correlationId, Optional<ByteString> key, Optional<ByteString> value) {
        this.partitionId = partitionId;
        this.traceabilityId = traceabilityId;
        this.correlationId = correlationId;
        this.key = key;
        this.value = value;
    }

    public static SerializedKeyAndValue create(
            Optional<Integer> partitionId, Optional<String> traceabilityId, Optional<String> correlationId, Optional<ByteString> key, Optional<ByteString> value) {
        return new SerializedKeyAndValue(partitionId, traceabilityId, correlationId, key, value);
    }

    public Optional<Integer> getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(Optional<Integer> partitionId) {
        this.partitionId = partitionId;
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

    public Optional<ByteString> getKey() {
        return key;
    }

    public void setKey(Optional<ByteString> key) {
        this.key = key;
    }

    public Optional<ByteString> getValue() {
        return value;
    }

    public void setValue(Optional<ByteString> value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializedKeyAndValue that = (SerializedKeyAndValue) o;
        return Objects.equals(partitionId, that.partitionId) && Objects.equals(traceabilityId, that.traceabilityId) && Objects.equals(correlationId, that.correlationId) && Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionId, traceabilityId, correlationId, key, value);
    }
}
