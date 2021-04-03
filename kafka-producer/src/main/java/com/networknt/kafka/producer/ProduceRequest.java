package com.networknt.kafka.producer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.networknt.kafka.entity.EmbeddedFormat;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProduceRequest {
    @JsonProperty("key_format")
    Optional<EmbeddedFormat> keyFormat;
    @JsonProperty("key_schema")
    Optional<String> keySchema;
    @JsonProperty("key_schema_id")
    Optional<Integer> keySchemaId;
    @JsonProperty("key_schema_version")
    Optional<Integer> keySchemaVersion;
    @JsonProperty("key_schema_subject")
    Optional<String> keySchemaSubject;

    @JsonProperty("value_format")
    Optional<EmbeddedFormat> valueFormat;
    @JsonProperty("value_schema")
    Optional<String> valueSchema;
    @JsonProperty("value_schema_id")
    Optional<Integer> valueSchemaId;
    @JsonProperty("value_schema_version")
    Optional<Integer> valueSchemaVersion;
    @JsonProperty("value_schema_subject")
    Optional<String> valueSchemaSubject;

    @JsonProperty("records")
    List<ProduceRecord> records;

    public ProduceRequest() {
    }

    public ProduceRequest(Optional<EmbeddedFormat> keyFormat,
                          Optional<String> keySchema,
                          Optional<Integer> keySchemaId,
                          Optional<Integer> keySchemaVersion,
                          Optional<String> keySchemaSubject,
                          Optional<EmbeddedFormat> valueFormat,
                          Optional<String> valueSchema,
                          Optional<Integer> valueSchemaId,
                          Optional<Integer> valueSchemaVersion,
                          Optional<String> valueSchemaSubject,
                          List<ProduceRecord> records) {
        this.keyFormat = keyFormat;
        this.keySchema = keySchema;
        this.keySchemaId = keySchemaId;
        this.keySchemaVersion = keySchemaVersion;
        this.keySchemaSubject = keySchemaSubject;
        this.valueFormat = valueFormat;
        this.valueSchema = valueSchema;
        this.valueSchemaId = valueSchemaId;
        this.valueSchemaVersion = valueSchemaVersion;
        this.valueSchemaSubject = valueSchemaSubject;
        this.records = records;
    }


    public static ProduceRequest create(
            EmbeddedFormat keyFormat,
            String keySchema,
            Integer keySchemaId,
            Integer keySchemaVersion,
            String keySchemaSubject,
            EmbeddedFormat valueFormat,
            String valueSchema,
            Integer valueSchemaId,
            Integer valueSchemaVersion,
            String valueSchemaSubject,
            List<ProduceRecord> records) {
        return new ProduceRequest(
                Optional.ofNullable(keyFormat),
                Optional.ofNullable(keySchema),
                Optional.ofNullable(keySchemaId),
                Optional.ofNullable(keySchemaVersion),
                Optional.ofNullable(keySchemaSubject),
                Optional.ofNullable(valueFormat),
                Optional.ofNullable(valueSchema),
                Optional.ofNullable(valueSchemaId),
                Optional.ofNullable(valueSchemaVersion),
                Optional.ofNullable(valueSchemaSubject),
                records);
    }

    @JsonCreator
    static ProduceRequest fromJson(
            @JsonProperty("records") List<ProduceRecord> records,
            @JsonProperty("key_format") EmbeddedFormat keyFormat,
            @JsonProperty("key_schema_id") Integer keySchemaId,
            @JsonProperty("key_schema") String keySchema,
            @JsonProperty("key_schema_version") Integer keySchemaVersion,
            @JsonProperty("key_schema_subject") String keySchemaSubject,
            @JsonProperty("value_format") EmbeddedFormat valueFormat,
            @JsonProperty("value_schema_id") Integer valueSchemaId,
            @JsonProperty("value_schema") String valueSchema,
            @JsonProperty("value_schema_version") Integer valueSchemaVersion,
            @JsonProperty("value_schema_subject") String valueSchemaSubject
            ) {
        return create(
                keyFormat,
                keySchema,
                keySchemaId,
                keySchemaVersion,
                keySchemaSubject,
                valueFormat,
                valueSchema,
                valueSchemaId,
                valueSchemaVersion,
                valueSchemaSubject,
                records != null ? records : List.of()
        );
    }

    public Optional<EmbeddedFormat> getKeyFormat() {
        return keyFormat;
    }

    public void setKeyFormat(Optional<EmbeddedFormat> keyFormat) {
        this.keyFormat = keyFormat;
    }

    public Optional<String> getKeySchema() {
        return keySchema;
    }

    public void setKeySchema(Optional<String> keySchema) {
        this.keySchema = keySchema;
    }

    public Optional<Integer> getKeySchemaId() {
        return keySchemaId;
    }

    public void setKeySchemaId(Optional<Integer> keySchemaId) {
        this.keySchemaId = keySchemaId;
    }

    public Optional<Integer> getKeySchemaVersion() {
        return keySchemaVersion;
    }

    public void setKeySchemaVersion(Optional<Integer> keySchemaVersion) {
        this.keySchemaVersion = keySchemaVersion;
    }

    public Optional<String> getKeySchemaSubject() {
        return keySchemaSubject;
    }

    public void setKeySchemaSubject(Optional<String> keySchemaSubject) {
        this.keySchemaSubject = keySchemaSubject;
    }

    public Optional<EmbeddedFormat> getValueFormat() {
        return valueFormat;
    }

    public void setValueFormat(Optional<EmbeddedFormat> valueFormat) {
        this.valueFormat = valueFormat;
    }

    public Optional<String> getValueSchema() {
        return valueSchema;
    }

    public void setValueSchema(Optional<String> valueSchema) {
        this.valueSchema = valueSchema;
    }

    public Optional<Integer> getValueSchemaId() {
        return valueSchemaId;
    }

    public void setValueSchemaId(Optional<Integer> valueSchemaId) {
        this.valueSchemaId = valueSchemaId;
    }

    public Optional<Integer> getValueSchemaVersion() {
        return valueSchemaVersion;
    }

    public void setValueSchemaVersion(Optional<Integer> valueSchemaVersion) {
        this.valueSchemaVersion = valueSchemaVersion;
    }

    public Optional<String> getValueSchemaSubject() {
        return valueSchemaSubject;
    }

    public void setValueSchemaSubject(Optional<String> valueSchemaSubject) {
        this.valueSchemaSubject = valueSchemaSubject;
    }

    public List<ProduceRecord> getRecords() {
        return records;
    }

    public void setRecords(List<ProduceRecord> records) {
        this.records = records;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProduceRequest that = (ProduceRequest) o;
        return Objects.equals(keyFormat, that.keyFormat) && Objects.equals(keySchema, that.keySchema) && Objects.equals(keySchemaId, that.keySchemaId) && Objects.equals(keySchemaVersion, that.keySchemaVersion) && Objects.equals(keySchemaSubject, that.keySchemaSubject) && Objects.equals(valueFormat, that.valueFormat) && Objects.equals(valueSchema, that.valueSchema) && Objects.equals(valueSchemaId, that.valueSchemaId) && Objects.equals(valueSchemaVersion, that.valueSchemaVersion) && Objects.equals(valueSchemaSubject, that.valueSchemaSubject) && Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyFormat, keySchema, keySchemaId, keySchemaVersion, keySchemaSubject, valueFormat, valueSchema, valueSchemaId, valueSchemaVersion, valueSchemaSubject, records);
    }
}
