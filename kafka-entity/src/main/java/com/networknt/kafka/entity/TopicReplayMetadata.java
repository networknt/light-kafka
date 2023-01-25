package com.networknt.kafka.entity;

public class TopicReplayMetadata {

    private String topicName;
    private boolean dlqIndicator;
    private int partition;
    private long offset;
    private String dlqConsumerGroup;
    private int timeout;
    private boolean lastRetry;

    public String getDlqConsumerGroup() {
        return dlqConsumerGroup;
    }

    public void setDlqConsumerGroup(String dlqConsumerGroup) {
        this.dlqConsumerGroup = dlqConsumerGroup;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isLastRetry() {
        return lastRetry;
    }

    public void setLastRetry(boolean lastRetry) {
        this.lastRetry = lastRetry;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public boolean isDlqIndicator() {
        return dlqIndicator;
    }

    public void setDlqIndicator(boolean dlqIndicator) {
        this.dlqIndicator = dlqIndicator;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
