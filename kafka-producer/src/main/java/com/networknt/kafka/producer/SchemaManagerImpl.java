package com.networknt.kafka.producer;

import com.networknt.exception.FrameworkException;
import com.networknt.status.Status;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.subject.strategy.SubjectNameStrategy;
import org.apache.kafka.common.errors.SerializationException;
import com.networknt.kafka.entity.EmbeddedFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public class SchemaManagerImpl implements SchemaManager {
    private static final Logger logger = LoggerFactory.getLogger(SchemaManagerImpl.class);
    private static final String FORMAT_WITH_SCHEMA_ID = "ERR12209";
    private static final String VERSION_WITH_SCHEMA_ID = "ERR12210";
    private static final String SCHEMA_WITH_SCHEMA_ID = "ERR12211";
    private static final String FORMAT_WITH_SCHEMA_VERSION = "ERR12212";
    private static final String SCHEMA_WITH_SCHEMA_VERSION = "ERR12213";
    private static final String RAW_SCHEMA_WITHOUT_FORMAT = "ERR12214";
    private static final String FORMAT_WITH_SUBJECT = "ERR12215";

    private final SchemaRegistryClient schemaRegistryClient;
    private final SubjectNameStrategy defaultSubjectNameStrategy;

    public SchemaManagerImpl(
            SchemaRegistryClient schemaRegistryClient,
            SubjectNameStrategy defaultSubjectNameStrategy) {
        this.schemaRegistryClient = requireNonNull(schemaRegistryClient);
        this.defaultSubjectNameStrategy = requireNonNull(defaultSubjectNameStrategy);
    }

    @Override
    public RegisteredSchema getSchema(
            String topicName,
            Optional<EmbeddedFormat> format,
            Optional<String> subject,
            Optional<SubjectNameStrategy> subjectNameStrategy,
            Optional<Integer> schemaId,
            Optional<Integer> schemaVersion,
            Optional<String> rawSchema,
            boolean isKey) {
        // (subject|subjectNameStrategy)?, schemaId
        if (schemaId.isPresent()) {
            if(schemaVersion.isPresent()) {
                Status status = new Status(VERSION_WITH_SCHEMA_ID, isKey ? "key" : "value");
                throw new FrameworkException(status);
            }
            if(rawSchema.isPresent()) {
                Status status = new Status(SCHEMA_WITH_SCHEMA_ID, isKey ? "key" : "value");
                throw new FrameworkException(status);
            }
            return getSchemaFromSchemaId(
                    topicName,
                    subject,
                    subjectNameStrategy,
                    schemaId.get(),
                    isKey);
        }

        // (subject|subjectNameStrategy)?, schemaVersion
        if (schemaVersion.isPresent()) {
            if(rawSchema.isPresent()) {
                Status status = new Status(SCHEMA_WITH_SCHEMA_VERSION, isKey ? "key" : "value");
                throw new FrameworkException(status);
            }
            return getSchemaFromSchemaVersion(
                    topicName,
                    subject,
                    subjectNameStrategy,
                    schemaVersion.get(),
                    isKey);
        }

        // format, (subject|subjectNameStrategy)?, rawSchema
        if (rawSchema.isPresent()) {
            if(!format.isPresent()) {
                Status status = new Status(RAW_SCHEMA_WITHOUT_FORMAT, isKey ? "key" : "value");
                throw new FrameworkException(status);
            }
            return getSchemaFromRawSchema(
                    topicName,
                    format.get(),
                    subject,
                    subjectNameStrategy,
                    rawSchema.get(),
                    isKey);
        }

        return findLatestSchema(topicName, subject, subjectNameStrategy, isKey);
    }

    private RegisteredSchema getSchemaFromSchemaId(
            String topicName,
            Optional<String> subject,
            Optional<SubjectNameStrategy> subjectNameStrategy,
            int schemaId,
            boolean isKey) {
        ParsedSchema schema;
        try {
            schema = schemaRegistryClient.getSchemaById(schemaId);
        } catch (IOException | RestClientException e) {
            // don't want to output a lot of errors when schema is not defined.
            if(logger.isDebugEnabled()) logger.debug("getSchemaById: ", e);
            throw new SerializationException(
                    String.format("Error when fetching schema by id. schemaId = %d", schemaId), e);
        }

        String actualSubject =
                subject.orElse(
                        subjectNameStrategy.orElse(defaultSubjectNameStrategy)
                                .subjectName(topicName, isKey, schema));

        int schemaVersion = getSchemaVersion(actualSubject, schema);

        return new RegisteredSchema(actualSubject, schemaId, schemaVersion, schema);
    }

    private int getSchemaVersion(String subject, ParsedSchema schema) {
        try {
            return schemaRegistryClient.getVersion(subject, schema);
        } catch (IOException | RestClientException e) {
            // don't want to output a lot of errors when schema is not defined.
            if(logger.isDebugEnabled()) logger.debug("getSchemaVersion: ", e);
            throw new SerializationException(
                    String.format(
                            "Error when fetching schema version. subject = %s, schema = %s",
                            subject, schema.canonicalString()),
                    e);
        }
    }

    private RegisteredSchema getSchemaFromSchemaVersion(
            String topicName,
            Optional<String> subject,
            Optional<SubjectNameStrategy> subjectNameStrategy,
            int schemaVersion,
            boolean isKey) {
        String actualSubject =
                subject.orElse(getSchemaSubjectUnsafe(topicName, isKey, subjectNameStrategy));

        Schema schema =
                schemaRegistryClient.getByVersion(
                        actualSubject, schemaVersion, /* lookupDeletedSchema= */ false);

        ParsedSchema parsedSchema =
                EmbeddedFormat.forSchemaType(schema.getSchemaType())
                        .getSchemaProvider()
                        .parseSchema(schema.getSchema(), schema.getReferences(), /* isNew= */ false)
                        .orElseThrow(
                                () ->
                                        new SerializationException(
                                                String.format(
                                                        "Error when fetching schema by version. subject = %s, version = %d",
                                                        actualSubject, schemaVersion)));

        return new RegisteredSchema(
                schema.getSubject(), schema.getId(), schemaVersion, parsedSchema);
    }

    private RegisteredSchema getSchemaFromRawSchema(
            String topicName,
            EmbeddedFormat format,
            Optional<String> subject,
            Optional<SubjectNameStrategy> subjectNameStrategy,
            String rawSchema,
            boolean isKey) {
        checkArgument(format.requiresSchema(), "%s does not support schemas.", format);

        ParsedSchema schema =
                format.getSchemaProvider()
                        .parseSchema(rawSchema, /* references= */ emptyList(), /* isNew= */ true)
                        .orElseThrow(
                                () ->
                                        new SerializationException(
                                                String.format(
                                                        "Error when parsing raw schema. format = %s, schema = %s",
                                                        format, rawSchema)));

        String actualSubject =
                subject.orElse(
                        subjectNameStrategy.orElse(defaultSubjectNameStrategy)
                                .subjectName(topicName, isKey, schema));

        int schemaId;
        try {
            try {
                // Check if the schema already exists first.
                schemaId = schemaRegistryClient.getId(actualSubject, schema);
            } catch (IOException | RestClientException e) {
                // don't want to output a lot of errors when schema is not defined.
                if(logger.isDebugEnabled()) logger.debug("getSchemaFromRawSchema: ", e);
                // Could not find the schema. We try to register the schema in that case.
                schemaId = schemaRegistryClient.register(actualSubject, schema);
            }
        } catch (IOException | RestClientException e) {
            throw new SerializationException(
                    String.format(
                            "Error when registering schema. format = %s, subject = %s, schema = %s",
                            format, actualSubject, schema.canonicalString()),
                    e);
        }

        int schemaVersion = getSchemaVersion(actualSubject, schema);

        return new RegisteredSchema(actualSubject, schemaId, schemaVersion, schema);
    }

    private RegisteredSchema findLatestSchema(
            String topicName,
            Optional<String> subject,
            Optional<SubjectNameStrategy> subjectNameStrategy,
            boolean isKey) {
        String actualSubject =
                subject.orElse(getSchemaSubjectUnsafe(topicName, isKey, subjectNameStrategy));

        SchemaMetadata metadata;
        try {
            metadata = schemaRegistryClient.getLatestSchemaMetadata(actualSubject);
        } catch (IOException | RestClientException e) {
            // don't want to output a lot of errors when schema is not defined.
            if(logger.isDebugEnabled()) logger.debug("findLatestSchema: ", e);
            throw new SerializationException(
                    String.format("Error when fetching latest schema version. subject = %s", actualSubject),
                    e);
        }

        ParsedSchema schema =
                EmbeddedFormat.forSchemaType(metadata.getSchemaType())
                        .getSchemaProvider()
                        .parseSchema(metadata.getSchema(), metadata.getReferences(), /* isNew= */ false)
                        .orElseThrow(
                                () ->
                                        new SerializationException(
                                                String.format(
                                                        "Error when fetching latest schema version. subject = %s",
                                                        actualSubject)));

        return new RegisteredSchema(actualSubject, metadata.getId(), metadata.getVersion(), schema);
    }

    /**
     * Tries to get the schema subject from only schema_subject_strategy, {@code topicName} and {@code
     * isKey}.
     *
     * <p>This operation  is only really supported if schema_subject_strategy does not depend on the
     * parsed schema to generate the subject name, as we need the subject name to fetch the schema
     * by version. That's the case, for example, of TopicNameStrategy
     * (schema_subject_strategy=TOPIC_NAME). Since TopicNameStrategy is so popular, instead of
     * requiring users to always specify schema_subject if using schema_version?, we try using the
     * strategy to generate the subject name, and fail if that does not work out.
     */
    private String getSchemaSubjectUnsafe(
            String topicName, boolean isKey, Optional<SubjectNameStrategy> subjectNameStrategy) {
        SubjectNameStrategy strategy = subjectNameStrategy.orElse(defaultSubjectNameStrategy);

        String subject = null;
        Exception cause = null;
        try {
            subject = strategy.subjectName(topicName, isKey, /* schema= */ null);
        } catch (Exception e) {
            // don't want to output a lot of errors when schema is not defined.
            if(logger.isDebugEnabled()) logger.debug("getSchemaSubjectUnsafe: ", e);
            cause = e;
        }

        if (subject == null) {
            IllegalArgumentException error =
                    new IllegalArgumentException(
                            String.format(
                                    "Cannot use%s schema_subject_strategy%s without schema_id or schema.",
                                    subjectNameStrategy.map(requestStrategy -> "").orElse(" default"),
                                    subjectNameStrategy.map(requestStrategy -> "=" + strategy).orElse("")));
            if (cause != null) {
                error.initCause(cause);
            }
            throw error;
        }

        return subject;
    }

}
