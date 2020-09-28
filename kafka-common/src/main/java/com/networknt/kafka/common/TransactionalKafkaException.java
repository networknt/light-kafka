package com.networknt.kafka.common;

public class TransactionalKafkaException extends Exception {
    private static final long serialVersionUID = 1L;

    public TransactionalKafkaException() {
        super();
    }

    public TransactionalKafkaException(String message) {
        super(message);
    }

    public TransactionalKafkaException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionalKafkaException(Throwable cause) {
        super(cause);
    }

}
