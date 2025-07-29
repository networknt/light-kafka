package com.networknt.kafka.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import com.networknt.config.Config;
import com.networknt.config.JsonMapper;
import com.networknt.kafka.common.config.KafkaConsumerConfig;

import java.util.*;

public class SidecarConsumerRecord {
    private final static KafkaConsumerConfig config = (KafkaConsumerConfig) Config.getInstance().getJsonObjectConfig(KafkaConsumerConfig.CONFIG_NAME, KafkaConsumerConfig.class);

    private final String topic;

    private final Object key;

    private final Object value;

    private final Map<String, String> headers;

    private final Integer partition;

    private final Long offset;
    private final Long timestamp;

    @JsonCreator
    private SidecarConsumerRecord(
            @JsonProperty("topic") String topic,
            @JsonProperty("key") Object key,
            @JsonProperty("value") Object value,
            @JsonProperty("headers") Map<String, String> headers,
            @JsonProperty("partition") Integer partition,
            @JsonProperty("offset") Long offset,
            @JsonProperty("timestamp") Long timestamp) {
        this.topic = topic;
        this.key = key;
        this.value = value;
        this.headers = headers;
        this.partition = partition;
        this.offset = offset;
        this.timestamp=timestamp;
    }

    @JsonProperty
    public String getTopic() {
        return topic;
    }

    @JsonProperty
    public Object getKey() {
        if(key != null) {
            switch (config.getKeyFormat()) {
                case "binary":
                    return new String(Base64.getEncoder().encode((byte[])key));
                case "string":
                case "json":
                case "avro":
                case "jsonschema":
                case "protobuf":
                    return key;
            }
        }
        return null;
    }

    @JsonProperty
    public Object getValue() {
        if(value != null) {
            switch (config.getValueFormat()) {
                case "binary":
                    return new String(Base64.getEncoder().encode((byte[])value));
                case "string":
                case "json":
                case "avro":
                case "jsonschema":
                case "protobuf":
                    return value;
            }
        }
        return null;
    }

    @JsonProperty
    public Map<String, String> getHeaders() { return headers; }

    @JsonProperty
    public Integer getPartition() {
        return partition;
    }

    @JsonProperty
    public Long getOffset() {
        return offset;
    }
    @JsonProperty
    public Long getTimestamp() {
        return timestamp;
    }

    public static SidecarConsumerRecord fromConsumerRecord(
            ConsumerRecord<Object, Object> record) {
        if (record.getPartition() < 0) {
            throw new IllegalArgumentException();
        }
        if (record.getOffset() < 0) {
            throw new IllegalArgumentException();
        }
        Object k = null;
        Object v = null;
        if(record.getKey() != null) {
            switch (config.getKeyFormat()) {
                case "binary":
                    k = ((ByteString)record.getKey()).toByteArray();
                    break;
                case "string":
                case "json":
                case "avro":
                case "jsonschema":
                case "protobuf":
                    k = record.getKey();
                    break;
            }
        }
        if(record.getValue() != null) {
            switch (config.getValueFormat()) {
                case "binary":
                    v = ((ByteString)record.getValue()).toByteArray();
                    break;
                case "string":
                case "json":
                case "avro":
                case "jsonschema":
                case "protobuf":
                    v = record.getValue();
                    break;
            }
        }

        return new SidecarConsumerRecord(
                Objects.requireNonNull(record.getTopic()),
                k,
                v,
                record.getHeaders(),
                record.getPartition(),
                record.getOffset(),
                record.getTimestamp());
    }

    public ConsumerRecord<Object, Object> toConsumerRecord() {
        if (topic == null) {
            throw new IllegalStateException();
        }
        if (partition == null || partition < 0) {
            throw new IllegalStateException();
        }
        if (offset == null || offset < 0) {
            throw new IllegalStateException();
        }

        Object k = null;
        Object v = null;
        if(key != null) {
            switch (config.getKeyFormat()) {
                case "binary":
                    k = ByteString.copyFrom((byte[])key);
                    break;
                case "string":
                case "json":
                case "avro":
                case "jsonschema":
                case "protobuf":
                    k = key;
                    break;
            }
        }
        if(value != null) {
            switch (config.getValueFormat()) {
                case "binary":
                    v = ByteString.copyFrom((byte[])value);
                    break;
                case "string":
                case "json":
                case "avro":
                case "jsonschema":
                case "protobuf":
                    v = value;
                    break;
            }
        }

        return ConsumerRecord.create(
                topic,
                k,
                v,
                headers,
                partition,
                offset,
                timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SidecarConsumerRecord that = (SidecarConsumerRecord) o;
        return Objects.equals(topic, that.topic)
                && Objects.equals(key, that.key)
                && Objects.equals(value, that.value)
                && Objects.equals(headers, that.headers)
                && Objects.equals(partition, that.partition)
                && Objects.equals(offset, that.offset);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(topic, key, value, headers, partition, offset, timestamp);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SidecarConsumerRecord.class.getSimpleName() + "[", "]")
                .add("topic='" + topic + "'")
                .add("key='" + key + "'")
                .add("value='" + value + "'")
                .add("headers" + JsonMapper.toJson(headers))
                .add("partition=" + partition)
                .add("offset=" + offset)
                .add("timestamp=" + timestamp)
                .toString();
    }

}
