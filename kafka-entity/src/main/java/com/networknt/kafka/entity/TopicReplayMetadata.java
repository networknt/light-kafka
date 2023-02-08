package com.networknt.kafka.entity;

public class TopicReplayMetadata {

    private String topicName;
    private boolean dlqIndicator;
    private int partition;
    private long startOffset;
    private long endOffset;
    private String consumerGroup;
    private int timeout;
    private boolean lastRetry;

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
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

    public long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    public long getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(long endOffset) {
        this.endOffset = endOffset;
    }
}
