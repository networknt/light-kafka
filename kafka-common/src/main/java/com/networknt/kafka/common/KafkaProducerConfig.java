package com.networknt.kafka.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.networknt.config.Config;
import com.networknt.config.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.networknt.server.ModuleRegistry;

import java.util.HashMap;
import java.util.Map;

import static com.networknt.kafka.common.KafkaConfigUtils.copyProperty;
import static com.networknt.kafka.common.KafkaConfigUtils.getFromMappedConfigAsType;
import static com.networknt.kafka.common.KafkaConfigUtils.getJsonMapConfig;
import static com.networknt.kafka.common.KafkaConfigUtils.normalizeKafkaProperties;
import static com.networknt.kafka.common.KafkaConfigUtils.sameMappedConfig;

/**
 * A Kafka setting configuration file. It get from defined resource yml file in
 * resources/config folder or externalized config folder. This config is for both
 * producer and consumer and for each individual application, it might have only
 * producer or consumer depending on the nature of the application.
 *
 * @author Steve Hu
 */
@ConfigSchema(
        configKey = "kafka-producer",
        configName = "kafka-producer",
        outputFormats = {
                OutputFormat.JSON_SCHEMA,
                OutputFormat.YAML
        }
)
public class KafkaProducerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerConfig.class);

    public static final String CONFIG_NAME = "kafka-producer";
    public static final String AUDIT_TARGET_TOPIC = "topic";
    public static final String AUDIT_TARGET_LOGFILE = "logfile";
    private static final String PROPERTIES_KEY = "properties";
    private static final String TOPIC_KEY = "topic";
    private static final String INJECT_OPEN_TRACING_KEY = "injectOpenTracing";
    private static final String KEY_FORMAT_KEY = "keyFormat";
    private static final String VALUE_FORMAT_KEY = "valueFormat";
    private static final String INJECT_CALLER_ID_KEY = "injectCallerId";
    private static final String AUDIT_ENABLED_KEY = "auditEnabled";
    private static final String AUDIT_TARGET_KEY = "auditTarget";
    private static final String AUDIT_TOPIC_KEY = "auditTopic";

    @ObjectField(
            configFieldName = PROPERTIES_KEY,
            useSubObjectDefault = true,
            ref = KafkaProducerPropertiesConfig.class,
            description = "Generic configuration for Kafka producer."
    )
    @JsonProperty(PROPERTIES_KEY)
    // This typed field is used by schema generation. Runtime loading retains the raw,
    // open-ended Kafka property map required by the 2.3.0-compatible accessors.
    private KafkaProducerPropertiesConfig propertiesConfig = new KafkaProducerPropertiesConfig();

    private Map<String, Object> properties;

    @StringField(
            configFieldName = TOPIC_KEY,
            externalizedKeyName = TOPIC_KEY,
            defaultValue = "portal-event",
            description = "The default topic for the producer. Only certain producer implementation will use it."
    )
    @JsonProperty(TOPIC_KEY)
    private String topic;

    @StringField(
            configFieldName = KEY_FORMAT_KEY,
            externalizedKeyName = KEY_FORMAT_KEY,
            defaultValue = "jsonschema",
            description = "Default key format if no schema for the topic value"
    )
    @JsonProperty(KEY_FORMAT_KEY)
    private String keyFormat;

    @StringField(
            configFieldName = VALUE_FORMAT_KEY,
            externalizedKeyName = VALUE_FORMAT_KEY,
            defaultValue = "jsonschema",
            description = "Default value format if no schema for the topic value"
    )
    @JsonProperty(VALUE_FORMAT_KEY)
    private String valueFormat;

    @BooleanField(
            configFieldName = INJECT_OPEN_TRACING_KEY,
            externalizedKeyName = INJECT_OPEN_TRACING_KEY,
            defaultValue = "false",
            description = "If open tracing is enable. traceability, correlation and metrics should not be in the chain if opentracing is used."
    )
    @JsonProperty(INJECT_OPEN_TRACING_KEY)
    private Boolean injectOpenTracing;

    @BooleanField(
            configFieldName = INJECT_CALLER_ID_KEY,
            externalizedKeyName = INJECT_CALLER_ID_KEY,
            defaultValue = "false",
            description = "Inject serviceId as callerId into the http header for metrics to collect the caller. The serviceId is from server.yml"
    )
    @JsonProperty(INJECT_CALLER_ID_KEY)
    private Boolean injectCallerId;

    @BooleanField(
            configFieldName = AUDIT_ENABLED_KEY,
            externalizedKeyName = AUDIT_ENABLED_KEY,
            defaultValue = "true",
            description = "If audit is enabled, the producer will send the audit message to the audit topic."
    )
    @JsonProperty(AUDIT_ENABLED_KEY)
    private Boolean auditEnabled;

    @StringField(
            configFieldName = AUDIT_TARGET_KEY,
            externalizedKeyName = AUDIT_TARGET_KEY,
            defaultValue = "logfile",
            description = "Audit log destination topic or logfile. Default to topic"
    )
    @JsonProperty(AUDIT_TARGET_KEY)
    private String auditTarget;

    @StringField(
            configFieldName = AUDIT_TOPIC_KEY,
            externalizedKeyName = AUDIT_TOPIC_KEY,
            defaultValue = "sidecar-audit",
            description = "The consumer audit topic name if the auditTarget is topic"
    )
    @JsonProperty(AUDIT_TOPIC_KEY)
    private String auditTopic;

    private Map<String, Object> mappedConfig;
    private static volatile KafkaProducerConfig instance;

    public KafkaProducerConfig() {
    }

    public KafkaProducerConfig(final String configName) {
        this(configName, getJsonMapConfig(configName, null));
    }

    private KafkaProducerConfig(final String configName, final Map<String, Object> mappedConfig) {
        this.topic = "portal-event";
        this.keyFormat = "jsonschema";
        this.valueFormat = "jsonschema";
        this.injectOpenTracing = false;
        this.injectCallerId = false;
        this.auditEnabled = true;
        this.auditTarget = "logfile";
        this.auditTopic = "sidecar-audit";
        this.mappedConfig = mappedConfig;
        this.setConfigData();
    }

    public static KafkaProducerConfig load() {
        return load(CONFIG_NAME);
    }

    public static KafkaProducerConfig load(final String configName) {
        if (CONFIG_NAME.equals(configName)) {
            Map<String, Object> mappedConfig = getJsonMapConfig(configName,
                    instance == null ? null : instance.getMappedConfig());
            if (instance != null && sameMappedConfig(instance.getMappedConfig(), mappedConfig)) {
                return instance;
            }
            synchronized (KafkaProducerConfig.class) {
                if (instance != null && sameMappedConfig(instance.getMappedConfig(), mappedConfig)) {
                    return instance;
                }
                instance = new KafkaProducerConfig(configName, mappedConfig);
                ModuleRegistry.registerModule(CONFIG_NAME, KafkaProducerConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), null);
                return instance;
            }
        }
        return new KafkaProducerConfig(configName);
    }

    public static void reload() {
        reload(CONFIG_NAME);
    }

    public static void reload(String configName) {
        if (CONFIG_NAME.equals(configName)) {
            synchronized (KafkaProducerConfig.class) {
                instance = new KafkaProducerConfig(configName, Config.getInstance().getJsonMapConfigNoCache(configName));
                ModuleRegistry.registerModule(CONFIG_NAME, KafkaProducerConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), null);
            }
        }
    }

    private void setConfigData() {
        if (this.mappedConfig.containsKey(PROPERTIES_KEY)) {
            this.properties = normalizeKafkaProperties(this.mappedConfig.get(PROPERTIES_KEY));
        } else {
            this.propertiesConfig = null;
            this.properties = getLegacyKafkaProperties(this.mappedConfig);
        }
        this.topic = getIfPresent(TOPIC_KEY, String.class, this.topic);
        this.keyFormat = getIfPresent(KEY_FORMAT_KEY, String.class, this.keyFormat);
        this.valueFormat = getIfPresent(VALUE_FORMAT_KEY, String.class, this.valueFormat);
        this.injectOpenTracing = getIfPresent(INJECT_OPEN_TRACING_KEY, Boolean.class, this.injectOpenTracing);
        this.injectCallerId = getIfPresent(INJECT_CALLER_ID_KEY, Boolean.class, this.injectCallerId);
        this.auditEnabled = getIfPresent(AUDIT_ENABLED_KEY, Boolean.class, this.auditEnabled);
        this.auditTarget = getIfPresent(AUDIT_TARGET_KEY, String.class, this.auditTarget);
        this.auditTopic = getIfPresent(AUDIT_TOPIC_KEY, String.class, this.auditTopic);
    }

    private <T> T getIfPresent(final String key, final Class<T> type, final T defaultValue) {
        return this.mappedConfig.containsKey(key)
                ? getFromMappedConfigAsType(this.mappedConfig, Config.getInstance().getMapper(), key, type)
                : defaultValue;
    }

    private static Map<String, Object> getLegacyKafkaProperties(final Map<String, Object> mappedConfig) {
        Map<String, Object> properties = new HashMap<>();
        copyProperty(mappedConfig, properties, "keySerializer", "key.serializer");
        copyProperty(mappedConfig, properties, "valueSerializer", "value.serializer");
        copyProperty(mappedConfig, properties, "acks", "acks");
        copyProperty(mappedConfig, properties, "bootstrapServers", "bootstrap.servers");
        copyProperty(mappedConfig, properties, "bufferMemory", "buffer.memory");
        copyProperty(mappedConfig, properties, "retries", "retries");
        copyProperty(mappedConfig, properties, "batchSize", "batch.size");
        copyProperty(mappedConfig, properties, "lingerMs", "linger.ms");
        copyProperty(mappedConfig, properties, "maxInFlightRequestsPerConnection", "max.in.flight.requests.per.connection");
        copyProperty(mappedConfig, properties, "enableIdempotence", "enable.idempotence");
        copyProperty(mappedConfig, properties, "transactionId", "transactional.id");
        copyProperty(mappedConfig, properties, "transactionTimeoutMs", "transaction.timeout.ms");
        copyProperty(mappedConfig, properties, "transactionalIdExpirationMs", "transactional.id.expiration.ms");
        copyProperty(mappedConfig, properties, "schemaRegistryUrl", "schema.registry.url");
        copyProperty(mappedConfig, properties, "schemaRegistryCache", "schema.registry.cache");
        copyProperty(mappedConfig, properties, "maxRequestSize", "max.request.size");
        return properties;
    }

    public Map<String, Object> getMappedConfig() {
        return mappedConfig;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties == null ? null : normalizeKafkaProperties(properties);
    }

    public Map<String, Object> getKafkaMapProperties() {
        return properties;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKeyFormat() {
        return keyFormat;
    }

    public void setKeyFormat(String keyFormat) {
        this.keyFormat = keyFormat;
    }

    public String getValueFormat() {
        return valueFormat;
    }

    public void setValueFormat(String valueFormat) {
        this.valueFormat = valueFormat;
    }

    public boolean isInjectOpenTracing() {
        return Boolean.TRUE.equals(injectOpenTracing);
    }

    public void setInjectOpenTracing(boolean injectOpenTracing) {
        this.injectOpenTracing = injectOpenTracing;
    }

    public boolean isInjectCallerId() {
        return Boolean.TRUE.equals(injectCallerId);
    }

    public void setInjectCallerId(boolean injectCallerId) {
        this.injectCallerId = injectCallerId;
    }

    public boolean isAuditEnabled() {
        return Boolean.TRUE.equals(auditEnabled);
    }

    public void setAuditEnabled(boolean auditEnabled) {
        this.auditEnabled = auditEnabled;
    }

    public String getAuditTarget() {
        return auditTarget;
    }

    public void setAuditTarget(String auditTarget) {
        this.auditTarget = auditTarget;
    }

    public String getAuditTopic() {
        return auditTopic;
    }

    public void setAuditTopic(String auditTopic) {
        this.auditTopic = auditTopic;
    }

    public Boolean getInjectOpenTracing() {
        return injectOpenTracing;
    }

    public Boolean getInjectCallerId() {
        return injectCallerId;
    }

    public Boolean getAuditEnabled() {
        return auditEnabled;
    }
}
