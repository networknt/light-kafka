package com.networknt.kafka.common;

import java.util.Properties;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;

public class KafkaConsumerConfig {
    public static final String CONFIG_NAME = "kafka-consumer";

    private String bootstrapServers;
    private String maxPollRecords;
    private String isolationLevel;
    private boolean enableAutoCommit;
    private int autoCommitIntervalMs;
    private String autoOffsetReset;
    private String keyDeserializer;
    private String valueDeserializer;
    private String groupId;
    private int maxConsumerThreads;
    private String serverId;
    private long requestMaxBytes;
    private int requestTimeoutMs;
    private int instanceTimeoutMs;
    private int fetchMinBytes;
    private int iteratorBackoffMs;
    private String schemaRegistryUrl;

    public KafkaConsumerConfig() {
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getMaxPollRecords() {
        return maxPollRecords;
    }

    public void setMaxPollRecords(String maxPollRecords) {
        this.maxPollRecords = maxPollRecords;
    }

    public String getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(String isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public boolean isEnableAutoCommit() {
        return enableAutoCommit;
    }

    public void setEnableAutoCommit(boolean enableAutoCommit) {
        this.enableAutoCommit = enableAutoCommit;
    }

    public String getKeyDeserializer() {
        return keyDeserializer;
    }

    public void setKeyDeserializer(String keyDeserializer) {
        this.keyDeserializer = keyDeserializer;
    }

    public String getValueDeserializer() {
        return valueDeserializer;
    }

    public void setValueDeserializer(String valueDeserializer) {
        this.valueDeserializer = valueDeserializer;
    }

    public int getAutoCommitIntervalMs() {
        return autoCommitIntervalMs;
    }

    public void setAutoCommitIntervalMs(int autoCommitIntervalMs) {
        this.autoCommitIntervalMs = autoCommitIntervalMs;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public void setAutoOffsetReset(String autoOffsetReset) {
        this.autoOffsetReset = autoOffsetReset;
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

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    public Properties getConsumerProperties() {
        Properties consumerProps = new Properties();
        consumerProps.setProperty(BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        consumerProps.setProperty(MAX_POLL_RECORDS_CONFIG, getMaxPollRecords());
        consumerProps.setProperty("schema.registry.url", getSchemaRegistryUrl());
        // TODO add other properties
        return consumerProps;
    }

}
