package com.networknt.kafka.entity;

public class RecordProcessedResult {
    ConsumerRecord record;
    boolean processed;
    String stacktrace;
    // The sidecar will try to get the correlationId from the record header with name X-Correlation-Id and fall
    // back to this value if record header doesn't have it. The backend API will add this if the original producer
    // is not adding the header to the producer record. The traceabilityId is the same.
    String correlationId;
    String traceabilityId;
    // For Auditing purpose, we need a unique identifier to identify the message for a particular transaction. The
    // backend need to set this up for the audit record. It might be the record key in a readable format or one of
    // the values from the value to identify the transaction.
    String key;


    public RecordProcessedResult() {
    }

    public RecordProcessedResult(ConsumerRecord record, boolean processed, String stacktrace, String correlationId, String traceabilityId, String key) {
        this.record = record;
        this.processed = processed;
        this.stacktrace = stacktrace;
        this.correlationId = correlationId;
        this.traceabilityId = traceabilityId;
        this.key = key;
    }

    public ConsumerRecord getRecord() {
        return record;
    }

    public void setRecord(ConsumerRecord record) {
        this.record = record;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
