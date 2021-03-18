package com.networknt.kafka.producer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.networknt.kafka.entity.EmbeddedFormat;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ProduceRequest {

    @JsonProperty("format")
    EmbeddedFormat format;
    @JsonProperty("key_schema")
    Optional<String> keySchema;
    @JsonProperty("key_schema_id")
    Optional<Integer> keySchemaId;
    @JsonProperty("value_schema")
    Optional<String> valueSchema;
    @JsonProperty("value_schema_id")
    Optional<Integer> valueSchemaId;
    @JsonProperty("records")
    List<ProduceRecord> records;

    public ProduceRequest() {
    }

    public ProduceRequest(EmbeddedFormat format, Optional<String> keySchema, Optional<Integer> keySchemaId, Optional<String> valueSchema, Optional<Integer> valueSchemaId, List<ProduceRecord> records) {
        this.format = format;
        this.keySchema = keySchema;
        this.keySchemaId = keySchemaId;
        this.valueSchema = valueSchema;
        this.valueSchemaId = valueSchemaId;
        this.records = records;
    }

    public static ProduceRequest create(EmbeddedFormat format, List<ProduceRecord> records) {
        return create(
                format,
                records,
                /* keySchemaId= */ null,
                /* keySchema= */ null,
                /* valueSchemaId= */ null,
                /* valueSchema= */ null);
    }

    public static ProduceRequest create(
            EmbeddedFormat format,
            List<ProduceRecord> records,
            @Nullable Integer keySchemaId,
            @Nullable String keySchema,
            @Nullable Integer valueSchemaId,
            @Nullable String valueSchema) {
        return new ProduceRequest(
                format,
                Optional.ofNullable(keySchema),
                Optional.ofNullable(keySchemaId),
                Optional.ofNullable(valueSchema),
                Optional.ofNullable(valueSchemaId),
                records);
    }

    @JsonCreator
    static ProduceRequest fromJson(
            @JsonProperty("format") @Nullable EmbeddedFormat format,
            @JsonProperty("records") @Nullable List<ProduceRecord> records,
            @JsonProperty("key_schema_id") @Nullable Integer keySchemaId,
            @JsonProperty("key_schema") @Nullable String keySchema,
            @JsonProperty("value_schema_id") @Nullable Integer valueSchemaId,
            @JsonProperty("value_schema") @Nullable String valueSchema) {
        return create(
                format != null ? format : EmbeddedFormat.JSONSCHEMA,
                records != null ? records : List.of(),
                keySchemaId,
                keySchema,
                valueSchemaId,
                valueSchema);
    }

    public EmbeddedFormat getFormat() {
        return format;
    }

    public void setFormat(EmbeddedFormat format) {
        this.format = format;
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
        return format == that.format && Objects.equals(keySchema, that.keySchema) && Objects.equals(keySchemaId, that.keySchemaId) && Objects.equals(valueSchema, that.valueSchema) && Objects.equals(valueSchemaId, that.valueSchemaId) && Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, keySchema, keySchemaId, valueSchema, valueSchemaId, records);
    }
}
