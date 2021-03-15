package com.networknt.kafka.producer;

import org.apache.kafka.clients.producer.RecordMetadata;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class ProduceResult {
    int partitionId;
    long offset;
    Instant timestamp;
    int serializeKeySize;
    int serializeValueSize;

    public ProduceResult() {
    }

    public ProduceResult(int partitionId, long offset, Instant timestamp, int serializeKeySize, int serializeValueSize) {
        this.partitionId = partitionId;
        this.offset = offset;
        this.timestamp = timestamp;
        this.serializeKeySize = serializeKeySize;
        this.serializeValueSize = serializeValueSize;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getSerializeKeySize() {
        return serializeKeySize;
    }

    public void setSerializeKeySize(int serializeKeySize) {
        this.serializeKeySize = serializeKeySize;
    }

    public int getSerializeValueSize() {
        return serializeValueSize;
    }

    public void setSerializeValueSize(int serializeValueSize) {
        this.serializeValueSize = serializeValueSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProduceResult that = (ProduceResult) o;
        return partitionId == that.partitionId && offset == that.offset && serializeKeySize == that.serializeKeySize && serializeValueSize == that.serializeValueSize && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionId, offset, timestamp, serializeKeySize, serializeValueSize);
    }

    public static ProduceResult fromRecordMetadata(RecordMetadata metadata) {
        return new ProduceResult(
                metadata.partition(),
                metadata.offset(),
                metadata.hasTimestamp() ? Instant.ofEpochMilli(metadata.timestamp()) : null,
                metadata.serializedKeySize(),
                metadata.serializedValueSize());
    }
}
