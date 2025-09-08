package com.networknt.kafka.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.networknt.config.schema.BooleanField;
import com.networknt.config.schema.MapField;
import com.networknt.config.schema.NumberField;
import com.networknt.config.schema.StringField;

import java.util.HashMap;
import java.util.Map;

import static com.networknt.kafka.common.config.KafkaConfigUtils.addIfSet;

public class KafkaConsumerPropertiesConfig {

    private static final String KEY_DESERIALIZER_KEY = "key.deserializer";
    private static final String VALUE_DESERIALIZER_KEY = "value.deserializer";
    private static final String FETCH_MAX_BYTES_KEY = "fetch.max.bytes";
    private static final String MAX_POLL_RECORDS_KEY = "max.poll.records";
    private static final String MAX_PARTITION_FETCH_BYTES_KEY = "max.partition.fetch.bytes";
    private static final String BOOTSTRAP_SERVERS_KEY = "bootstrap.servers";
    private static final String ENABLE_AUTO_COMMIT_KEY = "enable.auto.commit";
    private static final String AUTO_OFFSET_RESET_KEY = "auto.offset.reset";
    private static final String GROUP_ID_KEY = "group.id";
    private static final String SCHEMA_REGISTRY_URL_KEY = "schema.registry.url";
    private static final String SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY = "schema.registry.auto.register.schemas";
    private static final String SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY = "schema.registry.ssl.truststore.location";
    private static final String SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY = "schema.registry.ssl.truststore.password";
    private static final String ADDITIONAL_KAFKA_PROPERTIES_KEY = "additionalKafkaProperties";
    private static final String SECURITY_PROTOCOL_KEY = "security.protocol";
    private static final String SASL_MECHANISM_KEY = "sasl.mechanism";
    private static final String SASL_JAAS_CONFIG_KEY = "sasl.jaas.config";
    private static final String SSL_TRUSTSTORE_LOCATION_KEY = "ssl.truststore.location";
    private static final String SSL_TRUSTSTORE_PASSWORD_KEY = "ssl.truststore.password";
    private static final String SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_KEY = "ssl.endpoint.identification.algorithm";
    private static final String CLIENT_RACK_KEY = "client.rack";
    private static final String BASIC_AUTH_USER_INFO_KEY = "basic.auth.user.info";
    private static final String BASIC_AUTH_CREDENTIALS_SOURCE_KEY = "basic.auth.credentials.source";


    @StringField(
            configFieldName = BOOTSTRAP_SERVERS_KEY,
            externalizedKeyName = BOOTSTRAP_SERVERS_KEY,
            externalized = true,
            defaultValue = "localhost:9092",
            description = "Kafka bootstrap servers. Default to localhost:9092"
    )
    @JsonProperty(BOOTSTRAP_SERVERS_KEY)
    private String bootstrapServers;

    @StringField(
            configFieldName = KEY_DESERIALIZER_KEY,
            externalizedKeyName = KEY_DESERIALIZER_KEY,
            externalized = true,
            defaultValue = "org.apache.kafka.common.serialization.ByteArrayDeserializer",
            description = "Consumer will use the schema for deserialization from byte array\n" +
                    "Kafka key deserializer. Default to ByteArrayDeserializer"
    )
    @JsonProperty(KEY_DESERIALIZER_KEY)
    private String keyDeserializer;

    @StringField(
            configFieldName = VALUE_DESERIALIZER_KEY,
            externalizedKeyName = VALUE_DESERIALIZER_KEY,
            externalized = true,
            defaultValue = "org.apache.kafka.common.serialization.ByteArrayDeserializer",
            description = "Kafka value deserializer. Default to ByteArrayDeserializer"
    )
    @JsonProperty(VALUE_DESERIALIZER_KEY)
    private String valueDeserializer;

    @BooleanField(
            configFieldName = ENABLE_AUTO_COMMIT_KEY,
            externalizedKeyName = ENABLE_AUTO_COMMIT_KEY,
            externalized = true,
            defaultValue = "false",
            description = "As the control pane or API to access admin endpoint for commit, this value should be false."
    )
    @JsonProperty(ENABLE_AUTO_COMMIT_KEY)
    private Boolean enableAutoCommit;

    @StringField(
            configFieldName = AUTO_OFFSET_RESET_KEY,
            externalizedKeyName = AUTO_OFFSET_RESET_KEY,
            externalized = true,
            defaultValue = "earliest",
            description = "Kafka auto offset reset. Default to earliest"
    )
    @JsonProperty(AUTO_OFFSET_RESET_KEY)
    private String autoOffsetReset;

    @StringField(
            configFieldName = GROUP_ID_KEY,
            externalizedKeyName = GROUP_ID_KEY,
            externalized = true,
            defaultValue = "group1",
            description = "Kafka consumer group id. Default to group1"
    )
    @JsonProperty(GROUP_ID_KEY)
    private String groupId;

    @StringField(
            configFieldName = SCHEMA_REGISTRY_URL_KEY,
            externalizedKeyName = SCHEMA_REGISTRY_URL_KEY,
            externalized = true,
            defaultValue = "http://localhost:8081",
            description = "Schema registry url"
    )
    @JsonProperty(SCHEMA_REGISTRY_URL_KEY)
    private String schemaRegistryUrl;

    @BooleanField(
            configFieldName = SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY,
            externalizedKeyName = SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY,
            externalized = true,
            defaultValue = "true",
            description = "Schema registry auto register schema indicator for streams application. " +
                    "If true, the first request will register the schema auto automatically."
    )
    @JsonProperty(SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY)
    private Boolean schemaRegistryAutoRegisterSchemas;

    @StringField(
            configFieldName = SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY,
            externalizedKeyName = SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY,
            externalized = true,
            defaultValue = "/config/client.truststore",
            description = "Schema registry client truststore location, use the following two properties only if schema registry url is https."
    )
    @JsonProperty(SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY)
    private String schemaRegistrySslTruststoreLocation;

    @StringField(
            configFieldName = SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY,
            externalizedKeyName = SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY,
            externalized = true,
            defaultValue = "password",
            description = "Schema registry client truststore password"
    )
    @JsonProperty(SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY)
    private String schemaRegistrySslTruststorePassword;

    @StringField(
            configFieldName = SECURITY_PROTOCOL_KEY,
            externalizedKeyName = SECURITY_PROTOCOL_KEY,
            externalized = true,
            defaultValue = "SASL_SSL",
            description = "security configuration for enterprise deployment"
    )
    @JsonProperty(SECURITY_PROTOCOL_KEY)
    private String securityProtocol;

    @StringField(
            configFieldName = SASL_MECHANISM_KEY,
            externalizedKeyName = SASL_MECHANISM_KEY,
            externalized = true,
            defaultValue = "PLAIN",
            description = "SASL mechanism for authentication"
    )
    @JsonProperty(SASL_MECHANISM_KEY)
    private String saslMechanism;

    @StringField(
            configFieldName = SASL_JAAS_CONFIG_KEY,
            externalizedKeyName = SASL_JAAS_CONFIG_KEY,
            externalized = true,
            defaultValue = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\\\"${kafka-consumer.username:username}\\\" password=\\\"${kafka-consumer.password:password}\\\";",
            description = "SASL JAAS configuration for authentication"
    )
    @JsonProperty(SASL_JAAS_CONFIG_KEY)
    private String saslJaasConfig;

    @StringField(
            configFieldName = SSL_TRUSTSTORE_LOCATION_KEY,
            externalizedKeyName = SSL_TRUSTSTORE_LOCATION_KEY,
            externalized = true,
            defaultValue = "/config/client.truststore",
            description = "SSL truststore location for secure communication"
    )
    @JsonProperty(SSL_TRUSTSTORE_LOCATION_KEY)
    private String sslTruststoreLocation;

    @StringField(
            configFieldName = SSL_TRUSTSTORE_PASSWORD_KEY,
            externalizedKeyName = SSL_TRUSTSTORE_PASSWORD_KEY,
            externalized = true,
            defaultValue = "password",
            description = "SSL truststore password for secure communication"
    )
    @JsonProperty(SSL_TRUSTSTORE_PASSWORD_KEY)
    private String sslTruststorePassword;

    @StringField(
            configFieldName = SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_KEY,
            externalizedKeyName = SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_KEY,
            externalized = true,
            defaultValue = "also-name",
            description = "SSL endpoint identification algorithm for secure communication. " +
                    "This is used to verify the hostname of the server against the certificate presented by the server."
    )
    @JsonProperty(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_KEY)
    private String sslEndpointIdentificationAlgorithm;

    @StringField(
            configFieldName = CLIENT_RACK_KEY,
            externalizedKeyName = CLIENT_RACK_KEY,
            externalized = true,
            defaultValue = "rack1",
            description = "Client rack identifier for Kafka consumer. Default to rack1"
    )
    @JsonProperty(CLIENT_RACK_KEY)
    private String clientRack = "rack1";

    // TODO - this is a hack way to support original configuration style. This should be updated when we move security out from root properties.
    @StringField(
            configFieldName = BASIC_AUTH_USER_INFO_KEY,
            externalizedKeyName = BASIC_AUTH_USER_INFO_KEY,
            externalized = false,
            defaultValue = "${kafka-consumer.username:username}:${KAFKA_CONSUMER_PASSWORD:password}",
            description = "basic authentication user:pass for the schema registry"
    )
    @JsonProperty(BASIC_AUTH_USER_INFO_KEY)
    private String basicAuthUserInfo;

    @StringField(
            configFieldName = BASIC_AUTH_CREDENTIALS_SOURCE_KEY,
            externalizedKeyName = BASIC_AUTH_CREDENTIALS_SOURCE_KEY,
            externalized = true,
            defaultValue = "USER_INFO",
            description = "basic authentication credentials source for the schema registry. Default to USER_INFO"
    )
    @JsonProperty(BASIC_AUTH_CREDENTIALS_SOURCE_KEY)
    private String basicAuthCredentialsSource;

    @NumberField(
            configFieldName = FETCH_MAX_BYTES_KEY,
            externalizedKeyName = FETCH_MAX_BYTES_KEY,
            externalized = true,
            defaultValue = "102400",
            description = "Max fetch size from Kafka cluster. Default 50mb is too big for cache consumption on the sidecar"
    )
    @JsonProperty(FETCH_MAX_BYTES_KEY)
    private Integer fetchMaxBytes;

    @NumberField(
            configFieldName = MAX_POLL_RECORDS_KEY,
            externalizedKeyName = MAX_POLL_RECORDS_KEY,
            externalized = true,
            defaultValue = "100",
            description = "max poll records default is 500. Adjust it based on the size of the records to make sure each poll\n" +
                    "is similar to requestMaxBytes down below."
    )
    @JsonProperty(MAX_POLL_RECORDS_KEY)
    private Integer maxPollRecords;


    @NumberField(
            configFieldName = MAX_PARTITION_FETCH_BYTES_KEY,
            externalizedKeyName = MAX_PARTITION_FETCH_BYTES_KEY,
            externalized = true,
            defaultValue = "100",
            description = "The maximum amount of data per-partition the server will return. Records are fetched in batches by the consumer.\n" +
                    "If the first record batch in the first non-empty partition of the fetch is larger than this limit, the batch will still be returned to ensure that the consumer can make progress."
    )
    @JsonProperty(MAX_PARTITION_FETCH_BYTES_KEY)
    private Integer maxPartitionFetchBytes;

    @MapField(
            configFieldName = ADDITIONAL_KAFKA_PROPERTIES_KEY,
            externalizedKeyName = ADDITIONAL_KAFKA_PROPERTIES_KEY,
            externalized = true,
            description = "Any additional kafka properties that are not defined in the schema can be added here.\n" +
                    "This is useful for custom configurations that are not part of the standard Kafka consumer properties.",
            additionalProperties = true,

            // TODO - This is a bug with the light4j config schema generator. It should be able to handle Object.class, but it throws an error.
            valueType = String.class
    )
    @JsonProperty(ADDITIONAL_KAFKA_PROPERTIES_KEY)
    private final Map<String, Object> additionalKafkaProperties = new HashMap<>();

    public Map<String, Object> getMergedProperties() {
        Map<String, Object> properties = new HashMap<>(additionalKafkaProperties == null ? new HashMap<>() : additionalKafkaProperties);
        addIfSet(properties, BOOTSTRAP_SERVERS_KEY, bootstrapServers);
        addIfSet(properties, KEY_DESERIALIZER_KEY, keyDeserializer);
        addIfSet(properties, VALUE_DESERIALIZER_KEY, valueDeserializer);
        addIfSet(properties, ENABLE_AUTO_COMMIT_KEY, enableAutoCommit);
        addIfSet(properties, AUTO_OFFSET_RESET_KEY, autoOffsetReset);
        addIfSet(properties, GROUP_ID_KEY, groupId);
        addIfSet(properties, SCHEMA_REGISTRY_URL_KEY, schemaRegistryUrl);
        addIfSet(properties, SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY, schemaRegistryAutoRegisterSchemas);
        addIfSet(properties, SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY, schemaRegistrySslTruststoreLocation);
        addIfSet(properties, SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY, schemaRegistrySslTruststorePassword);
        addIfSet(properties, SECURITY_PROTOCOL_KEY, securityProtocol);
        addIfSet(properties, SASL_MECHANISM_KEY, saslMechanism);
        addIfSet(properties, SASL_JAAS_CONFIG_KEY, saslJaasConfig);
        addIfSet(properties, SSL_TRUSTSTORE_LOCATION_KEY, sslTruststoreLocation);
        addIfSet(properties, SSL_TRUSTSTORE_PASSWORD_KEY, sslTruststorePassword);
        addIfSet(properties, SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_KEY, sslEndpointIdentificationAlgorithm);
        addIfSet(properties, CLIENT_RACK_KEY, clientRack);
        addIfSet(properties, BASIC_AUTH_USER_INFO_KEY, basicAuthUserInfo);
        addIfSet(properties, BASIC_AUTH_CREDENTIALS_SOURCE_KEY, basicAuthCredentialsSource);
        addIfSet(properties, FETCH_MAX_BYTES_KEY, fetchMaxBytes);
        addIfSet(properties, MAX_POLL_RECORDS_KEY, maxPollRecords);
        addIfSet(properties, MAX_PARTITION_FETCH_BYTES_KEY, maxPartitionFetchBytes);
        return properties;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getKeyDeserializer() {
        return keyDeserializer;
    }

    public String getValueDeserializer() {
        return valueDeserializer;
    }

    public Boolean getEnableAutoCommit() {
        return enableAutoCommit;
    }

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public Boolean getSchemaRegistryAutoRegisterSchemas() {
        return schemaRegistryAutoRegisterSchemas;
    }

    public String getSchemaRegistrySslTruststoreLocation() {
        return schemaRegistrySslTruststoreLocation;
    }

    public String getSchemaRegistrySslTruststorePassword() {
        return schemaRegistrySslTruststorePassword;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public String getSaslMechanism() {
        return saslMechanism;
    }

    public String getSaslJaasConfig() {
        return saslJaasConfig;
    }

    public String getSslTruststoreLocation() {
        return sslTruststoreLocation;
    }

    public String getSslTruststorePassword() {
        return sslTruststorePassword;
    }

    public String getSslEndpointIdentificationAlgorithm() {
        return sslEndpointIdentificationAlgorithm;
    }

    public String getClientRack() {
        return clientRack;
    }

    public String getBasicAuthUserInfo() {
        return basicAuthUserInfo;
    }

    public String getBasicAuthCredentialsSource() {
        return basicAuthCredentialsSource;
    }

    public Integer getFetchMaxBytes() {
        return fetchMaxBytes;
    }

    public Integer getMaxPollRecords() {
        return maxPollRecords;
    }

    public Integer getMaxPartitionFetchBytes() {
        return maxPartitionFetchBytes;
    }

    public Map<String, Object> getAdditionalKafkaProperties() {
        return additionalKafkaProperties;
    }
}
