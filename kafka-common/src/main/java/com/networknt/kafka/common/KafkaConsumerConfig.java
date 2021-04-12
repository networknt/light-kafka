package com.networknt.kafka.common;

import java.util.Map;

public class KafkaConsumerConfig {
    public static final String CONFIG_NAME = "kafka-consumer";

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

    private Map<String, Object> properties;

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
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
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

    public void setBackendApiPath(String backendApiPath) {
        this.backendApiPath = backendApiPath;
    }
}
