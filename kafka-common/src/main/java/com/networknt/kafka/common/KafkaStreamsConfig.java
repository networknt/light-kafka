package com.networknt.kafka.common;

import java.util.Map;

public class KafkaStreamsConfig {
    public static final String CONFIG_NAME = "kafka-streams";
    boolean cleanUp;
    public static final String AUDIT_TARGET_TOPIC = "topic";
    public static final String AUDIT_TARGET_LOGFILE = "logfile";

    private boolean auditEnabled;
    private String auditTarget;
    private String auditTopic;
    private boolean deadLetterEnabled;
    private String deadLetterTopicExt;
    private Map<String, Object> properties;

    public KafkaStreamsConfig() {
    }

    public boolean isCleanUp() {
        return cleanUp;
    }

    public void setCleanUp(boolean cleanUp) {
        this.cleanUp = cleanUp;
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

    public String getAuditTopic() {
        return auditTopic;
    }

    public void setAuditTopic(String auditTopic) {
        this.auditTopic = auditTopic;
    }

    public boolean isDeadLetterEnabled() {
        return deadLetterEnabled;
    }

    public void setDeadLetterEnabled(boolean deadLetterEnabled) {
        this.deadLetterEnabled = deadLetterEnabled;
    }

    public String getDeadLetterTopicExt() {
        return deadLetterTopicExt;
    }

    public void setDeadLetterTopicExt(String deadLetterTopicExt) {
        this.deadLetterTopicExt = deadLetterTopicExt;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
