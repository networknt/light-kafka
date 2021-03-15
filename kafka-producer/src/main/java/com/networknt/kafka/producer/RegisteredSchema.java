package com.networknt.kafka.producer;

import io.confluent.kafka.schemaregistry.ParsedSchema;

import java.util.Objects;

public class RegisteredSchema {
    String subject;
    int schemaId;
    int schemaVersion;
    ParsedSchema schema;

    public final EmbeddedFormat getFormat() {
        return EmbeddedFormat.forSchemaType(getSchema().schemaType());
    }

    public RegisteredSchema(String subject, int schemaId, int schemaVersion, ParsedSchema schema) {
        this.subject = subject;
        this.schemaId = schemaId;
        this.schemaVersion = schemaVersion;
        this.schema = schema;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(int schemaId) {
        this.schemaId = schemaId;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public ParsedSchema getSchema() {
        return schema;
    }

    public void setSchema(ParsedSchema schema) {
        this.schema = schema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisteredSchema that = (RegisteredSchema) o;
        return schemaId == that.schemaId && schemaVersion == that.schemaVersion && Objects.equals(subject, that.subject) && Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, schemaId, schemaVersion, schema);
    }
}
