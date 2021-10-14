package com.networknt.kafka.common;

import org.apache.avro.specific.SpecificRecord;

public class EventNotification {
    long nonce;
    String app;
    String name;
    boolean success;
    String error;
    SpecificRecord event;

    public EventNotification(long nonce, String app, String name, boolean success, String error, SpecificRecord event) {
        this.nonce = nonce;
        this.app = app;
        this.name = name;
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

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                + ",\"app\":\"" + app + "\""
                + ",\"name\":\"" + name + "\""
                + ",\"success\":" + success + ","
                + ((error == null) ? "" : ("\"error\":\"" + error + "\","))
                + "\"event\":" + AvroConverter.toJson(event, false)
                + "}";
    }
}
