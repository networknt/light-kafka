package com.networknt.kafka.consumer.exception;

public class RollbackException extends RuntimeException {
    public RollbackException(String message) {
        super(message);
    }
}
