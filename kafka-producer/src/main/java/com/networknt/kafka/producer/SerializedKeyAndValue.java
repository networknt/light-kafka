package com.networknt.kafka.producer;

import com.google.protobuf.ByteString;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SerializedKeyAndValue {
    Optional<Integer> partitionId;
    Optional<String> traceabilityId;
    Optional<String> correlationId;
    Optional<ByteString> key;
    Optional<ByteString> value;
    Optional<Map<String, String>> headers;
    Optional<Long> timestamp;

    public SerializedKeyAndValue(Optional<Integer> partitionId, Optional<String> traceabilityId, Optional<String> correlationId, Optional<ByteString> key, Optional<ByteString> value,Optional<Map<String, String>> headers, Optional<Long> timestamp) {
        this.partitionId = partitionId;
        this.traceabilityId = traceabilityId;
        this.correlationId = correlationId;
        this.key = key;
        this.value = value;
        this.headers= headers;
        this.timestamp= timestamp;
    }

    public static SerializedKeyAndValue create(
            Optional<Integer> partitionId, Optional<String> traceabilityId, Optional<String> correlationId, Optional<ByteString> key, Optional<ByteString> value, Optional<Map<String, String>> headers, Optional<Long> timestamp) {
        return new SerializedKeyAndValue(partitionId, traceabilityId, correlationId, key, value, headers,timestamp);
    }

    public Optional<Long> getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Optional<Long> timestamp) {
        this.timestamp = timestamp;
    }
    public Optional<Map<String, String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Optional<Map<String, String>> headers) {
        this.headers = headers;
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
        return Objects.equals(partitionId, that.partitionId) && Objects.equals(traceabilityId, that.traceabilityId) && Objects.equals(correlationId, that.correlationId) && Objects.equals(key, that.key) && Objects.equals(value, that.value) && Objects.equals(headers, that.headers) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionId, traceabilityId, correlationId, key, value, headers, timestamp);
    }
}
