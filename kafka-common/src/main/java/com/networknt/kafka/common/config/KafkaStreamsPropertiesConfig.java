package com.networknt.kafka.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.networknt.config.schema.BooleanField;
import com.networknt.config.schema.MapField;
import com.networknt.config.schema.StringField;

import java.util.HashMap;
import java.util.Map;

import static com.networknt.kafka.common.config.KafkaConfigUtils.addIfSet;

public class KafkaStreamsPropertiesConfig {

    private static final String BOOTSTRAP_SERVERS_KEY = "bootstrap.servers";
    private static final String KEY_DESERIALIZER_KEY = "key.deserializer";
    private static final String VALUE_DESERIALIZER_KEY = "value.deserializer";
    private static final String AUTO_OFFSET_RESET_KEY = "auto.offset.reset";
    private static final String APPLICATION_ID_KEY = "application.id";
    private static final String SCHEMA_REGISTRY_URL_KEY = "schema.registry.url";
    private static final String SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY = "schema.registry.auto.register.schemas";
    private static final String STATE_DIR_KEY = "state.dir";
    private static final String ADDITIONAL_PROPERTIES_KEY = "additionalProperties";

    private static final String SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY = "schema.registry.ssl.truststore.location";
    private static final String SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY = "schema.registry.ssl.truststore.password";
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
            description = "Kafka key deserializer. Default to ByteArrayDeserializer"
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
            configFieldName = APPLICATION_ID_KEY,
            externalizedKeyName = APPLICATION_ID_KEY,
            externalized = true,
            defaultValue = "placeholder",
            description = "A unique application id for the Kafka streams app. You need to replace it or overwrite it in your code."
    )
    @JsonProperty(APPLICATION_ID_KEY)
    private String applicationId;

    @StringField(
            configFieldName = SCHEMA_REGISTRY_URL_KEY,
            externalizedKeyName = SCHEMA_REGISTRY_URL_KEY,
            externalized = true,
            defaultValue = "http://localhost:8081",
            description = "SSchema registry url"
    )
    @JsonProperty(SCHEMA_REGISTRY_URL_KEY)
    private String schemaRegistryUrl;

    @BooleanField(
            configFieldName = SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY,
            externalizedKeyName = SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY,
            externalized = true,
            defaultValue = "true",
            description = "Schema registry auto register schema indicator for streams application. If true, the first request will register the schema auto automatically."
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
            defaultValue = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${kafka-streams.username:username}\" password=\"${KAFKA_STREAMS_PASSWORD:password}\";",
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
            description = "Client rack identifier for Kafka streams. Default to rack1"
    )
    @JsonProperty(CLIENT_RACK_KEY)
    private String clientRack;

    // TODO - this is a hack way to support original configuration style. This should be updated when we move security out from root properties.
    @StringField(
            configFieldName = BASIC_AUTH_USER_INFO_KEY,
            externalizedKeyName = BASIC_AUTH_USER_INFO_KEY,
            externalized = false,
            defaultValue = "${kafka-streams.username:username}:${KAFKA_STREAMS_PASSWORD:password}",
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

    @StringField(
            configFieldName = STATE_DIR_KEY,
            externalizedKeyName = STATE_DIR_KEY,
            externalized = true,
            defaultValue = "/tmp",
            description = "The directory where Kafka Streams state is stored. Default to /tmp"
    )
    @JsonProperty(STATE_DIR_KEY)
    private String stateDir;

    @MapField(
            configFieldName = ADDITIONAL_PROPERTIES_KEY,
            externalizedKeyName = ADDITIONAL_PROPERTIES_KEY,
            externalized = true,
            description = "Any additional properties that are not defined in the schema can be added here.\n" +
                    "This is useful for custom configurations that are not part of the standard Kafka streams properties.",
            additionalProperties = true,

            // TODO - This is a bug with the light4j config schema generator. It should be able to handle Object.class, but it throws an error.
            valueType = String.class
    )
    @JsonProperty(ADDITIONAL_PROPERTIES_KEY)
    private Map<String, Object> additionalProperties = new HashMap<>();

    public Map<String, Object> getMergedProperties() {
        Map<String, Object> mergedProperties = new HashMap<>(additionalProperties);
        addIfSet(mergedProperties, BOOTSTRAP_SERVERS_KEY, bootstrapServers);
        addIfSet(mergedProperties, KEY_DESERIALIZER_KEY, keyDeserializer);
        addIfSet(mergedProperties, VALUE_DESERIALIZER_KEY, valueDeserializer);
        addIfSet(mergedProperties, AUTO_OFFSET_RESET_KEY, autoOffsetReset);
        addIfSet(mergedProperties, APPLICATION_ID_KEY, applicationId);
        addIfSet(mergedProperties, SCHEMA_REGISTRY_URL_KEY, schemaRegistryUrl);
        addIfSet(mergedProperties, SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY, schemaRegistryAutoRegisterSchemas);
        addIfSet(mergedProperties, SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY, schemaRegistrySslTruststoreLocation);
        addIfSet(mergedProperties, SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY, schemaRegistrySslTruststorePassword);
        addIfSet(mergedProperties, SECURITY_PROTOCOL_KEY, securityProtocol);
        addIfSet(mergedProperties, SASL_MECHANISM_KEY, saslMechanism);
        addIfSet(mergedProperties, SASL_JAAS_CONFIG_KEY, saslJaasConfig);
        addIfSet(mergedProperties, SSL_TRUSTSTORE_LOCATION_KEY, sslTruststoreLocation);
        addIfSet(mergedProperties, SSL_TRUSTSTORE_PASSWORD_KEY, sslTruststorePassword);
        addIfSet(mergedProperties, SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_KEY, sslEndpointIdentificationAlgorithm);
        addIfSet(mergedProperties, CLIENT_RACK_KEY, clientRack);
        addIfSet(mergedProperties, BASIC_AUTH_USER_INFO_KEY, basicAuthUserInfo);
        addIfSet(mergedProperties, BASIC_AUTH_CREDENTIALS_SOURCE_KEY, basicAuthCredentialsSource);
        addIfSet(mergedProperties, STATE_DIR_KEY, stateDir);
        return mergedProperties;
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

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public String getApplicationId() {
        return applicationId;
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

    public String getStateDir() {
        return stateDir;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
}
