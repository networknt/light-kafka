package com.networknt.kafka.common.config;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.networknt.config.schema.BooleanField;
import com.networknt.config.schema.MapField;
import com.networknt.config.schema.NumberField;
import com.networknt.config.schema.StringField;

import java.util.HashMap;
import java.util.Map;

import static com.networknt.kafka.common.config.KafkaConfigUtils.addIfSet;

public class KafkaProducerPropertiesConfig {

    private static final String KEY_SERIALIZER_KEY = "key.serializer";
    private static final String VALUE_SERIALIZER_KEY = "value.serializer";
    private static final String ACKS_KEY = "acks";
    private static final String BOOTSTRAP_SERVERS_KEY = "bootstrap.servers";
    private static final String BUFFER_MEMORY_KEY = "buffer.memory";
    private static final String RETRIES_KEY = "retries";
    private static final String BATCH_SIZE_KEY = "batch.size";
    private static final String LINGER_MS_KEY = "linger.ms";
    private static final String MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION_KEY = "max.in.flight.requests.per.connection";
    private static final String ENABLE_IDEMPOTENCE_KEY = "enable.idempotence";
    private static final String SCHEMA_REGISTRY_URL_KEY = "schema.registry.url";
    private static final String SCHEMA_REGISTRY_CACHE_KEY = "schema.registry.cache";
    private static final String SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY = "schema.registry.auto.register.schemas";
    private static final String MAX_REQUEST_SIZE_KEY = "max.request.size";
    private static final String ADDITIONAL_KAFKA_PROPERTIES_KEY = "additionalKafkaProperties";

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
            configFieldName = KEY_SERIALIZER_KEY,
            externalizedKeyName = KEY_SERIALIZER_KEY,
            defaultValue = "org.apache.kafka.common.serialization.ByteArraySerializer",
            description = "Kafka key serializer. Default to ByteArraySerializer"
    )
    @JsonProperty(KEY_SERIALIZER_KEY)
    private String keySerializer;

    @StringField(
            configFieldName = VALUE_SERIALIZER_KEY,
            externalizedKeyName = VALUE_SERIALIZER_KEY,
            defaultValue = "org.apache.kafka.common.serialization.ByteArraySerializer",
            description = "Kafka value serializer. Default to ByteArraySerializer"
    )
    @JsonProperty(VALUE_SERIALIZER_KEY)
    private String valueSerializer;

    @StringField(
            configFieldName = ACKS_KEY,
            externalizedKeyName = ACKS_KEY,
            defaultValue = "all",
            description = "This value is a string, if using 1 or 0, you must use '1' or '0' as the value"
    )
    @JsonProperty(ACKS_KEY)
    private String acks;

    @StringField(
            configFieldName = BOOTSTRAP_SERVERS_KEY,
            externalizedKeyName = BOOTSTRAP_SERVERS_KEY,
            defaultValue = "localhost:9092",
            description = "Kafka bootstrap servers. Default to localhost:9092"
    )
    @JsonProperty(BOOTSTRAP_SERVERS_KEY)
    private String bootstrapServers;

    @NumberField(
            configFieldName = BUFFER_MEMORY_KEY,
            externalizedKeyName = BUFFER_MEMORY_KEY,
            defaultValue = "33554432", // 32MB
            description = "Buffer size for unsent records. Default to 33554432"
    )
    @JsonProperty(BUFFER_MEMORY_KEY)
    private Integer bufferMemory;

    @NumberField(
            configFieldName = RETRIES_KEY,
            externalizedKeyName = RETRIES_KEY,
            defaultValue = "3",
            description = "Retry times for producer. Default to 3"
    )
    @JsonProperty(RETRIES_KEY)
    private Integer retries;

    @NumberField(
            configFieldName = BATCH_SIZE_KEY,
            externalizedKeyName = BATCH_SIZE_KEY,
            defaultValue = "16384",
            description = "Batch size. Default to 16KB"
    )
    @JsonProperty(BATCH_SIZE_KEY)
    private Integer batchSize;

    @NumberField(
            configFieldName = LINGER_MS_KEY,
            externalizedKeyName = LINGER_MS_KEY,
            defaultValue = "1",
            description = "Linger time. Default to 1ms"
    )
    @JsonProperty(LINGER_MS_KEY)
    private Integer lingerMs;

    @NumberField(
            configFieldName = MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION_KEY,
            externalizedKeyName = MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION_KEY,
            defaultValue = "5",
            description = "max in-flight requests per connection. Default to 5"
    )
    @JsonProperty(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION_KEY)
    private Integer maxInFlightRequestsPerConnection;

    @BooleanField(
            configFieldName = ENABLE_IDEMPOTENCE_KEY,
            externalizedKeyName = ENABLE_IDEMPOTENCE_KEY,
            defaultValue = "false",
            description = "enable idempotence. Default to true"
    )
    @JsonProperty(ENABLE_IDEMPOTENCE_KEY)
    private Boolean enableIdempotence;

    @StringField(
            configFieldName = SCHEMA_REGISTRY_URL_KEY,
            externalizedKeyName = SCHEMA_REGISTRY_URL_KEY,
            defaultValue = "http://localhost:8081",
            description = "Confluent schema registry url"
    )
    @JsonProperty(SCHEMA_REGISTRY_URL_KEY)
    private String schemaRegistryUrl;

    @NumberField(
            configFieldName = SCHEMA_REGISTRY_CACHE_KEY,
            externalizedKeyName = SCHEMA_REGISTRY_CACHE_KEY,
            defaultValue = "100",
            description = "Schema registry identity cache size"
    )
    @JsonProperty(SCHEMA_REGISTRY_CACHE_KEY)
    private Integer schemaRegistryCache;

    @BooleanField(
            configFieldName = SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY,
            externalizedKeyName = SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY,
            defaultValue = "true",
            description = "Schema registry auto register schema indicator for streams application.\n" +
                    "If true, the first request will register the schema auto automatically."
    )
    @JsonProperty(SCHEMA_REGISTRY_AUTO_REGISTER_SCHEMAS_KEY)
    private Boolean schemaRegistryAutoRegisterSchemas;

    @StringField(
            configFieldName = SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY,
            externalizedKeyName = SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY,
            defaultValue = "/config/client.truststore",
            description = "Schema registry client truststore location, use the following two properties only if schema registry url is https."
    )
    @JsonProperty(SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY)
    private String schemaRegistrySslTruststoreLocation;

    @StringField(
            configFieldName = SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY,
            externalizedKeyName = SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY,
            defaultValue = "password",
            description = "Schema registry client truststore password"
    )
    @JsonProperty(SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY)
    private String schemaRegistrySslTruststorePassword;

    @StringField(
            configFieldName = SECURITY_PROTOCOL_KEY,
            externalizedKeyName = SECURITY_PROTOCOL_KEY,
            defaultValue = "SASL_SSL",
            description = "security configuration for enterprise deployment"
    )
    @JsonProperty(SECURITY_PROTOCOL_KEY)
    private String securityProtocol;

    @StringField(
            configFieldName = SASL_MECHANISM_KEY,
            externalizedKeyName = SASL_MECHANISM_KEY,
            defaultValue = "PLAIN",
            description = "SASL mechanism for authentication"
    )
    @JsonProperty(SASL_MECHANISM_KEY)
    private String saslMechanism;

    @StringField(
            configFieldName = SASL_JAAS_CONFIG_KEY,
            externalizedKeyName = SASL_JAAS_CONFIG_KEY,
            defaultValue = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\\\"${kafka-producer.username:username}\\\" password=\\\"${kafka-producer.password:password}\\\";",
            description = "SASL JAAS configuration for authentication",
            injection = false
    )
    @JsonProperty(SASL_JAAS_CONFIG_KEY)
    private String saslJaasConfig;

    @StringField(
            configFieldName = SSL_TRUSTSTORE_LOCATION_KEY,
            externalizedKeyName = SSL_TRUSTSTORE_LOCATION_KEY,
            defaultValue = "/config/client.truststore",
            description = "SSL truststore location for secure communication"
    )
    @JsonProperty(SSL_TRUSTSTORE_LOCATION_KEY)
    private String sslTruststoreLocation;

    @StringField(
            configFieldName = SSL_TRUSTSTORE_PASSWORD_KEY,
            externalizedKeyName = SSL_TRUSTSTORE_PASSWORD_KEY,
            defaultValue = "password",
            description = "SSL truststore password for secure communication"
    )
    @JsonProperty(SSL_TRUSTSTORE_PASSWORD_KEY)
    private String sslTruststorePassword;

    @StringField(
            configFieldName = SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_KEY,
            externalizedKeyName = SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_KEY,
            defaultValue = "also-name",
            description = "SSL endpoint identification algorithm for secure communication. " +
                    "This is used to verify the hostname of the server against the certificate presented by the server."
    )
    @JsonProperty(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_KEY)
    private String sslEndpointIdentificationAlgorithm;

    @StringField(
            configFieldName = CLIENT_RACK_KEY,
            externalizedKeyName = CLIENT_RACK_KEY,
            defaultValue = "rack1",
            description = "Client rack identifier for Kafka producer. Default to rack1"
    )
    @JsonProperty(CLIENT_RACK_KEY)
    private String clientRack;

    // TODO - this is a hack way to support original configuration style. This should be updated when we move security out from root properties.
    @StringField(
            configFieldName = BASIC_AUTH_USER_INFO_KEY,
            externalizedKeyName = BASIC_AUTH_USER_INFO_KEY,
            defaultValue = "${kafka-producer.username:username}:${kafka-producer.password:password}",
            description = "basic authentication user:pass for the schema registry",
            injection = false
    )
    @JsonProperty(BASIC_AUTH_USER_INFO_KEY)
    private String basicAuthUserInfo;

    @StringField(
            configFieldName = BASIC_AUTH_CREDENTIALS_SOURCE_KEY,
            externalizedKeyName = BASIC_AUTH_CREDENTIALS_SOURCE_KEY,
            defaultValue = "USER_INFO",
            description = "basic authentication credentials source for the schema registry. Default to USER_INFO"
    )
    @JsonProperty(BASIC_AUTH_CREDENTIALS_SOURCE_KEY)
    private String basicAuthCredentialsSource;

    @NumberField(
            configFieldName = MAX_REQUEST_SIZE_KEY,
            externalizedKeyName = MAX_REQUEST_SIZE_KEY,
            defaultValue = "1048576",
            description = "If you have message that is bigger than 1 MB to produce, increase this value."
    )
    @JsonProperty(MAX_REQUEST_SIZE_KEY)
    private Integer maxRequestSize;

    @MapField(
            configFieldName = ADDITIONAL_KAFKA_PROPERTIES_KEY,
            externalizedKeyName = ADDITIONAL_KAFKA_PROPERTIES_KEY,
            description = "Any additional properties that are not defined in the schema can be added here.\n" +
                          "This is useful for custom configurations that are not part of the standard Kafka producer properties.",
            additionalProperties = true,

            // TODO - This is a bug with the light4j config schema generator. It should be able to handle Object.class, but it throws an error.
            valueType = String.class
    )
    @JsonProperty(ADDITIONAL_KAFKA_PROPERTIES_KEY)
    private final Map<String, Object> additionalKafkaProperties = new HashMap<>();

    public Map<String, Object> getMergedProperties() {
        Map<String, Object> properties = new HashMap<>(additionalKafkaProperties == null ? new HashMap<>() : additionalKafkaProperties);
        addIfSet(properties, KEY_SERIALIZER_KEY, keySerializer);
        addIfSet(properties, VALUE_SERIALIZER_KEY, valueSerializer);
        addIfSet(properties, ACKS_KEY, acks);
        addIfSet(properties, BOOTSTRAP_SERVERS_KEY, bootstrapServers);
        addIfSet(properties, BUFFER_MEMORY_KEY, bufferMemory);
        addIfSet(properties, RETRIES_KEY, retries);
        addIfSet(properties, BATCH_SIZE_KEY, batchSize);
        addIfSet(properties, LINGER_MS_KEY, lingerMs);
        addIfSet(properties, MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION_KEY, maxInFlightRequestsPerConnection);
        addIfSet(properties, ENABLE_IDEMPOTENCE_KEY, enableIdempotence);
        addIfSet(properties, SCHEMA_REGISTRY_URL_KEY, schemaRegistryUrl);
        addIfSet(properties, SCHEMA_REGISTRY_CACHE_KEY, schemaRegistryCache);
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
        addIfSet(properties, MAX_REQUEST_SIZE_KEY, maxRequestSize);
        return properties;
    }

    public String getKeySerializer() {
        return keySerializer;
    }

    public String getValueSerializer() {
        return valueSerializer;
    }

    public String getAcks() {
        return acks;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public Integer getBufferMemory() {
        return bufferMemory;
    }

    public Integer getRetries() {
        return retries;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public Integer getLingerMs() {
        return lingerMs;
    }

    public Integer getMaxInFlightRequestsPerConnection() {
        return maxInFlightRequestsPerConnection;
    }

    public Boolean getEnableIdempotence() {
        return enableIdempotence;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public Integer getSchemaRegistryCache() {
        return schemaRegistryCache;
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

    public Integer getMaxRequestSize() {
        return maxRequestSize;
    }

    public Map<String, Object> getAdditionalKafkaProperties() {
        return additionalKafkaProperties;
    }
}
