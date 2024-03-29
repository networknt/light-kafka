package com.networknt.kafka.common;

import java.util.Map;

/**
 * A Kafka setting configuration file. It get from defined resource yml file in
 * resources/config folder or externalized config folder. This config is for both
 * producer and consumer and for each individual application, it might have only
 * producer or consumer depending on the nature of the application.
 *
 * @author Steve Hu
 */
public class KafkaProducerConfig {
    public static final String CONFIG_NAME = "kafka-producer";
    public static final String AUDIT_TARGET_TOPIC = "topic";
    public static final String AUDIT_TARGET_LOGFILE = "logfile";

    private Map<String, Object> properties;

    private String topic;
    private boolean injectOpenTracing;
    private boolean injectCallerId;
    private boolean auditEnabled;
    private String auditTarget;
    private String auditTopic;
    private String keyFormat;
    private String valueFormat;

    public KafkaProducerConfig() {
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isInjectOpenTracing() {
        return injectOpenTracing;
    }

    public void setInjectOpenTracing(boolean injectOpenTracing) {
        this.injectOpenTracing = injectOpenTracing;
    }

    public boolean isInjectCallerId() {
        return injectCallerId;
    }

    public void setInjectCallerId(boolean injectCallerId) {
        this.injectCallerId = injectCallerId;
    }

    public String getAuditTopic() {
        return auditTopic;
    }

    public void setAuditTopic(String auditTopic) {
        this.auditTopic = auditTopic;
    }

    public boolean isAuditEnabled() {
        return auditEnabled;
    }

    public void setAuditEnabled(boolean auditEnabled) {
        this.auditEnabled = auditEnabled;
    }

    public String getAuditTarget() {
        return auditTarget;
    }

    public void setAuditTarget(String auditTarget) {
        this.auditTarget = auditTarget;
    }

    public String getKeyFormat() {
        return keyFormat;
    }

    public void setKeyFormat(String keyFormat) {
        this.keyFormat = keyFormat;
    }

    public String getValueFormat() {
        return valueFormat;
    }

    public void setValueFormat(String valueFormat) {
        this.valueFormat = valueFormat;
    }
}
