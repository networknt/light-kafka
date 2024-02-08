package com.networknt.kafka.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.networknt.utility.Constants;
import com.networknt.utility.ObjectUtils;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@JsonIgnoreProperties(value = {"headers"})
public interface StateStoreValue {

    public static final Logger logger = LoggerFactory.getLogger(StateStoreValue.class);

    void setCorrelationId(String correlationId);

    String getCorrelationId();

    void setTraceabilityId(String traceabilityId);

    String getTraceabilityId();

    default void setHeaders(Headers headers) {

        if (!ObjectUtils.isEmpty(headers.lastHeader(Constants.CORRELATION_ID_STRING))) {
            setCorrelationId(new String(headers.lastHeader(Constants.CORRELATION_ID_STRING).value(), StandardCharsets.UTF_8));
        } else {
            logger.error(Constants.CORRELATION_ID_STRING + " is not present in headers");
        }

        if (!ObjectUtils.isEmpty(headers.lastHeader(Constants.TRACEABILITY_ID_STRING))) {
            setTraceabilityId(new String(headers.lastHeader(Constants.TRACEABILITY_ID_STRING).value(), StandardCharsets.UTF_8));
        } else {
            logger.error(Constants.TRACEABILITY_ID_STRING + " is not present in headers");
        }


    }

    default Headers getHeaders() {
        Headers headers = new RecordHeaders();
        headers.add(Constants.CORRELATION_ID_STRING, getCorrelationId().getBytes(StandardCharsets.UTF_8));
        headers.add(Constants.TRACEABILITY_ID_STRING, getTraceabilityId().getBytes(StandardCharsets.UTF_8));

        return headers;
    }
}
