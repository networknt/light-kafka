package com.networknt.kafka.entity;

import java.util.Objects;

public class PartitionOffset {
    Integer partition;
    Long offset;
    Integer errorCode;
    String error;

    public PartitionOffset(Integer partition, Long offset, Integer errorCode, String error) {
        this.partition = partition;
        this.offset = offset;
        this.errorCode = errorCode;
        this.error = error;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartitionOffset that = (PartitionOffset) o;
        return Objects.equals(partition, that.partition) && Objects.equals(offset, that.offset) && Objects.equals(errorCode, that.errorCode) && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partition, offset, errorCode, error);
    }

    @Override
    public String toString() {
        return "PartitionOffset{" +
                "partition=" + partition +
                ", offset=" + offset +
                ", errorCode=" + errorCode +
                ", error='" + error + '\'' +
                '}';
    }
}
