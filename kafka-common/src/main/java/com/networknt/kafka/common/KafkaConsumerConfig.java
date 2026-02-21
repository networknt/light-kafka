package com.networknt.kafka.common;

import java.util.Map;

public class KafkaConsumerConfig {
    public static final String CONFIG_NAME = "kafka-consumer";
    public static final String AUDIT_TARGET_TOPIC = "topic";
    public static final String AUDIT_TARGET_LOGFILE = "logfile";
    private String groupId;
    private int maxConsumerThreads;
    private String serverId;
    private long requestMaxBytes;
    private int requestTimeoutMs;
    private int instanceTimeoutMs;
    private int fetchMinBytes;
    private int iteratorBackoffMs;
    private String topic;
    private int waitPeriod;
    private String keyFormat;
    private String valueFormat;
    private String backendApiHost;
    private String backendApiPath;
    private boolean deadLetterEnabled;
    private String deadLetterTopicExt;
    private boolean auditEnabled;
    private String auditTarget;
    private String auditTopic;
    private boolean useNoWrappingAvro;
    private boolean backendConnectionReset;
    private int batchRollbackThreshold;

    private Map<String, Object> properties;
    private Map<String, Object> additionalKafkaProperties;

    public KafkaConsumerConfig() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getMaxConsumerThreads() {
        return maxConsumerThreads;
    }

    public void setMaxConsumerThreads(int maxConsumerThreads) {
        this.maxConsumerThreads = maxConsumerThreads;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public long getRequestMaxBytes() {
        return requestMaxBytes;
    }

    public void setRequestMaxBytes(long requestMaxBytes) {
        this.requestMaxBytes = requestMaxBytes;
    }

    public int getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(int requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public int getInstanceTimeoutMs() {
        return instanceTimeoutMs;
    }

    public void setInstanceTimeoutMs(int instanceTimeoutMs) {
        this.instanceTimeoutMs = instanceTimeoutMs;
    }

    public int getFetchMinBytes() {
        return fetchMinBytes;
    }

    public void setFetchMinBytes(int fetchMinBytes) {
        this.fetchMinBytes = fetchMinBytes;
    }

    public int getIteratorBackoffMs() {
        return iteratorBackoffMs;
    }

    public void setIteratorBackoffMs(int iteratorBackoffMs) {
        this.iteratorBackoffMs = iteratorBackoffMs;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getWaitPeriod() {
        return waitPeriod;
    }

    public void setWaitPeriod(int waitPeriod) {
        this.waitPeriod = waitPeriod;
    }

    public Map<String, Object> getProperties() {
        return mergeAdditionalProperties();
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = mergeAdditionalProperties();
        this.groupId = (String)getProperties().get("group.id");
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

    public String getBackendApiHost() {
        return backendApiHost;
    }

    public void setBackendApiHost(String backendApiHost) {
        this.backendApiHost = backendApiHost;
    }

    public String getBackendApiPath() {
        return backendApiPath;
    }

    public boolean isDeadLetterEnabled() {
        return deadLetterEnabled;
    }

    public void setDeadLetterEnabled(boolean deadLetterEnabled) {
        this.deadLetterEnabled = deadLetterEnabled;
    }

    public void setBackendApiPath(String backendApiPath) {
        this.backendApiPath = backendApiPath;
    }

    public String getDeadLetterTopicExt() {
        return deadLetterTopicExt;
    }

    public void setDeadLetterTopicExt(String deadLetterTopicExt) {
        this.deadLetterTopicExt = deadLetterTopicExt;
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

    public boolean isUseNoWrappingAvro() {
        return useNoWrappingAvro;
    }

    public void setUseNoWrappingAvro(boolean useNoWrappingAvro) {
        this.useNoWrappingAvro = useNoWrappingAvro;
    }

    public boolean isBackendConnectionReset() { return backendConnectionReset; }

    public void setBackendConnectionReset(boolean backendConnectionReset) { this.backendConnectionReset = backendConnectionReset; }

    public int getBatchRollbackThreshold() {
        return batchRollbackThreshold;
    }
    public void setBatchRollbackThreshold(int batchRollbackThreshold) {
        this.batchRollbackThreshold = batchRollbackThreshold;
    }


    public Map<String, Object> getAdditionalKafkaProperties() {
        return additionalKafkaProperties;
    }

    public void setAdditionalKafkaProperties(Map<String, Object> additionalKafkaProperties) {
        this.additionalKafkaProperties = additionalKafkaProperties;
    }

    private Map mergeAdditionalProperties() {
        if (additionalKafkaProperties != null && properties != null) {
            properties.putAll(additionalKafkaProperties);
        }
        return properties;
    }


}
