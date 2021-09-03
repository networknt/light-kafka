package com.networknt.kafka.entity;

/**
 * This is the record for the audit entry for both producer and consumer.
 *
 * @author Steve Hu
 */
public class AuditRecord {
    public enum AuditType {
        PRODUCER, REACTIVE_CONSUMER, ACTIVE_CONSUMER
    }
    public enum AuditStatus {
        SUCCESS, FAILURE
    }

    // A randomly-generated UUID to ensure uniqueness across all sources
    String id;
    // The serviceId that produces the audit entry. The sidecar will use the backend serviceId in the server.yml
    String serviceId;
    // The audit type
    AuditType auditType;
    // The Kafka record for the audit
    String topic;
    int partition;
    long offset;
    // Distributed tracing info
    String correlationId;
    String traceabilityId;
    String key;
    AuditStatus auditStatus;
    String stacktrace;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public AuditType getAuditType() {
        return auditType;
    }

    public void setAuditType(AuditType auditType) {
        this.auditType = auditType;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getTraceabilityId() {
        return traceabilityId;
    }

    public void setTraceabilityId(String traceabilityId) {
        this.traceabilityId = traceabilityId;
    }

    public AuditStatus getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(AuditStatus auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
