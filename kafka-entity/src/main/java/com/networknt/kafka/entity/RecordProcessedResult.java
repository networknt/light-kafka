package com.networknt.kafka.entity;

public class RecordProcessedResult {
    ConsumerRecord record;
    boolean processed;
    String stacktrace;

    public RecordProcessedResult() {
    }

    public RecordProcessedResult(ConsumerRecord record, boolean processed, String stacktrace) {
        this.record = record;
        this.processed = processed;
        this.stacktrace = stacktrace;
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
}
