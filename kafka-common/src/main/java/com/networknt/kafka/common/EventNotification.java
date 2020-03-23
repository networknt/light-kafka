package com.networknt.kafka.common;

public class EventNotification {
    long nonce;
    boolean success;
    String error;
    String eventJson;

    public EventNotification(long nonce, boolean success, String error, String eventJson) {
        this.nonce = nonce;
        this.success = success;
        this.error = error;
        this.eventJson = eventJson;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getEventJson() {
        return eventJson;
    }

    public void setEventJson(String eventJson) {
        this.eventJson = eventJson;
    }
}
