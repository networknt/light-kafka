package com.networknt.kafka.entity;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeadLetterQueueReplayResponse  {

    private String description;
    private Long records;
    private java.util.List<String> topics;
    private String group;
    private String instance;

    public DeadLetterQueueReplayResponse () {
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("records")
    public Long getRecords() {
        return records;
    }

    public void setRecords(Long records) {
        this.records = records;
    }

    @JsonProperty("topics")
    public java.util.List<String> getTopics() {
        return topics;
    }

    public void setTopics(java.util.List<String> topics) {
        this.topics = topics;
    }

    @JsonProperty("group")
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @JsonProperty("instance")
    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeadLetterQueueReplayResponse DeadLetterQueueReplayResponse = (DeadLetterQueueReplayResponse) o;

        return Objects.equals(description, DeadLetterQueueReplayResponse.description) &&
               Objects.equals(records, DeadLetterQueueReplayResponse.records) &&
               Objects.equals(topics, DeadLetterQueueReplayResponse.topics) &&
               Objects.equals(group, DeadLetterQueueReplayResponse.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, records, topics, group);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeadLetterQueueReplayResponse {\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");        sb.append("    records: ").append(toIndentedString(records)).append("\n");        sb.append("    topics: ").append(toIndentedString(topics)).append("\n");        sb.append("    group: ").append(toIndentedString(group)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
