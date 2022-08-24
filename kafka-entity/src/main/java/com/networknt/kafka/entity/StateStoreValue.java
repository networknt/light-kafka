package com.networknt.kafka.entity;

import org.apache.kafka.common.header.Headers;

import java.util.Map;

public interface StateStoreValue {

    void setCorrelationId(String correlationId);
    String getCorrelationId();
    void setTraceabilityId(String traceabilityId);
    String getTraceabilityId();

    default void setHeader(Headers headers){

        setCorrelationId(headers.lastHeader("X-Correlation-Id").toString());
        setTraceabilityId(headers.lastHeader("X-Traceability-Id").toString());
    }



}

