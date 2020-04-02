package com.networknt.kafka.common;

import org.apache.avro.specific.SpecificRecord;

public class EventNotification {
    long nonce;
    boolean success;
    String error;
    SpecificRecord event;

    public EventNotification(long nonce, boolean success, String error, SpecificRecord event) {
        this.nonce = nonce;
        this.success = success;
        this.error = error;
        this.event = event;
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

    public SpecificRecord getEvent() {
        return event;
    }

    public void setEvent(SpecificRecord event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "{\"nonce\":" + nonce
                + ",\"success\":" + success + ","
                + ((error == null) ? "" : ("\"error\":\"" + error + "\","))
                + "\"event\":" + AvroConverter.toJson(event, false)
                + "}";
    }
}
