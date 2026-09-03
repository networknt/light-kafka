package com.networknt.kafka.common;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.networknt.config.Config;
import com.networknt.config.schema.*;

import com.networknt.server.ModuleRegistry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.networknt.kafka.common.KafkaConfigUtils.copyProperty;
import static com.networknt.kafka.common.KafkaConfigUtils.getFromMappedConfigAsType;
import static com.networknt.kafka.common.KafkaConfigUtils.getJsonMapConfig;
import static com.networknt.kafka.common.KafkaConfigUtils.normalizeKafkaProperties;
import static com.networknt.kafka.common.KafkaConfigUtils.sameMappedConfig;

@ConfigSchema(
        configName = "kafka-streams",
        configKey = "kafka-streams",
        outputFormats = {
                OutputFormat.JSON_SCHEMA,
                OutputFormat.YAML
        }
)
public class KafkaStreamsConfig extends KafkaConfigUtils {
    public static final String CONFIG_NAME = "kafka-streams";
    public static final String AUDIT_TARGET_TOPIC = "topic";
    public static final String AUDIT_TARGET_LOGFILE = "logfile";
    private static final List<String> MASKS = Arrays.asList("basic.auth.user.info", "sasl.jaas.config", "schema.registry.ssl.truststore.password");

    public static final String PROPERTIES_KEY = "properties";
    public static final String CLEAN_UP_KEY = "cleanUp";
    public static final String DEAD_LETTER_ENABLED_KEY = "deadLetterEnabled";
    public static final String DEAD_LETTER_TOPIC_EXT_KEY = "deadLetterTopicExt";
    private static final String AUDIT_ENABLED_KEY = "auditEnabled";
    private static final String AUDIT_TARGET_KEY = "auditTarget";
    private static final String AUDIT_TOPIC_KEY = "auditTopic";
    private static final String DEAD_LETTER_CONTROLLER_TOPIC_KEY = "deadLetterControllerTopic";


    @ObjectField(
            configFieldName = PROPERTIES_KEY,
            useSubObjectDefault = true,
            description = "Generic Kafka Streams Configuration",
            ref = KafkaStreamsPropertiesConfig.class
    )
    @JsonProperty(PROPERTIES_KEY)
    // This typed field is used by schema generation. Runtime loading retains the raw,
    // open-ended Kafka property map required by the 2.3.0-compatible accessors.
    private KafkaStreamsPropertiesConfig propertiesConfig = new KafkaStreamsPropertiesConfig();

    private Map<String, Object> properties;

    @BooleanField(
            configFieldName = CLEAN_UP_KEY,
            externalizedKeyName = CLEAN_UP_KEY,
            defaultValue = "false",
            description = "Only set to true right after the streams reset and start the server. Once the server is up, shutdown and change this to false and restart."
    )
    private Boolean cleanUp;

    @BooleanField(
            configFieldName = DEAD_LETTER_ENABLED_KEY,
            externalizedKeyName = DEAD_LETTER_ENABLED_KEY,
            defaultValue = "true",
            description = "Common configuration properties between active and reactive consumers\n" +
                    "Indicator if the dead letter topic is enabled."
    )
    @JsonProperty(DEAD_LETTER_ENABLED_KEY)
    private Boolean deadLetterEnabled;

    @StringField(
            configFieldName = DEAD_LETTER_TOPIC_EXT_KEY,
            externalizedKeyName = DEAD_LETTER_TOPIC_EXT_KEY,
            defaultValue = ".dlq",
            description = "The extension of the dead letter queue(topic) that is added to the original topic to form the dead letter topic"
    )
    @JsonProperty(DEAD_LETTER_TOPIC_EXT_KEY)
    private String deadLetterTopicExt;

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

    @StringField(
            configFieldName = DEAD_LETTER_CONTROLLER_TOPIC_KEY,
            externalizedKeyName = DEAD_LETTER_CONTROLLER_TOPIC_KEY,
            defaultValue = "dev.ent.all.kafka.replay.metadata.0",
            description = "The dead letter controller topic, one per environment."
    )
    @JsonProperty(DEAD_LETTER_CONTROLLER_TOPIC_KEY)
    private String deadLetterControllerTopicKey;



    private Map<String, Object> mappedConfig;
    private static volatile KafkaStreamsConfig instance;

    public KafkaStreamsConfig() {
    }

    public KafkaStreamsConfig(final String configName) {
        this(configName, getJsonMapConfig(configName, null));
    }

    private KafkaStreamsConfig(final String configName, final Map<String, Object> mappedConfig) {
        this.cleanUp = false;
        this.deadLetterEnabled = true;
        this.deadLetterTopicExt = ".dlq";
        this.auditEnabled = true;
        this.auditTarget = "logfile";
        this.auditTopic = "sidecar-audit";
        this.deadLetterControllerTopicKey = "dev.ent.all.kafka.replay.metadata.0";
        this.mappedConfig = mappedConfig;
        this.setConfigData();
    }

    public static KafkaStreamsConfig load() {
        return load(CONFIG_NAME);
    }

    public static KafkaStreamsConfig load(final String configName) {
        if (CONFIG_NAME.equals(configName)) {
            Map<String, Object> mappedConfig = getJsonMapConfig(configName,
                    instance == null ? null : instance.getMappedConfig());
            if (instance != null && sameMappedConfig(instance.getMappedConfig(), mappedConfig)) {
                return instance;
            }
            synchronized (KafkaStreamsConfig.class) {
                if (instance != null && sameMappedConfig(instance.getMappedConfig(), mappedConfig)) {
                    return instance;
                }
                instance = new KafkaStreamsConfig(configName, mappedConfig);
                ModuleRegistry.registerModule(CONFIG_NAME, KafkaStreamsConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), MASKS);
                return instance;
            }
        }
        return new KafkaStreamsConfig(configName);
    }

    public static void reload() {
        reload(CONFIG_NAME);
    }

    public static void reload(String configName) {
        if (CONFIG_NAME.equals(configName)) {
            synchronized (KafkaStreamsConfig.class) {
                instance = new KafkaStreamsConfig(configName, Config.getInstance().getJsonMapConfigNoCache(configName));
                ModuleRegistry.registerModule(CONFIG_NAME, KafkaStreamsConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), MASKS);
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
        this.cleanUp = getIfPresent(CLEAN_UP_KEY, Boolean.class, this.cleanUp);
        this.deadLetterEnabled = getIfPresent(DEAD_LETTER_ENABLED_KEY, Boolean.class, this.deadLetterEnabled);
        this.deadLetterTopicExt = getIfPresent(DEAD_LETTER_TOPIC_EXT_KEY, String.class, this.deadLetterTopicExt);
        this.auditEnabled = getIfPresent(AUDIT_ENABLED_KEY, Boolean.class, this.auditEnabled);
        this.auditTarget = getIfPresent(AUDIT_TARGET_KEY, String.class, this.auditTarget);
        this.auditTopic = getIfPresent(AUDIT_TOPIC_KEY, String.class, this.auditTopic);
        this.deadLetterControllerTopicKey = getIfPresent(DEAD_LETTER_CONTROLLER_TOPIC_KEY, String.class,
                this.deadLetterControllerTopicKey);
    }

    private <T> T getIfPresent(final String key, final Class<T> type, final T defaultValue) {
        return this.mappedConfig.containsKey(key)
                ? getFromMappedConfigAsType(this.mappedConfig, Config.getInstance().getMapper(), key, type)
                : defaultValue;
    }

    private static Map<String, Object> getLegacyKafkaProperties(final Map<String, Object> mappedConfig) {
        Map<String, Object> properties = new HashMap<>();
        copyProperty(mappedConfig, properties, "bootstrapServers", "bootstrap.servers");
        copyProperty(mappedConfig, properties, "keyDeserializer", "key.deserializer");
        copyProperty(mappedConfig, properties, "valueDeserializer", "value.deserializer");
        copyProperty(mappedConfig, properties, "autoOffsetReset", "auto.offset.reset");
        copyProperty(mappedConfig, properties, "applicationId", "application.id");
        copyProperty(mappedConfig, properties, "schemaRegistryUrl", "schema.registry.url");
        copyProperty(mappedConfig, properties, "stateDir", "state.dir");
        return properties;
    }

    public Map<String, Object> getMappedConfig() {
        return mappedConfig;
    }

    public Map<String, Object> getKafkaMapProperties() {
        return properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties == null ? null : normalizeKafkaProperties(properties);
    }

    public Boolean getCleanUp() {
        return cleanUp;
    }

    public boolean isCleanUp() {
        return Boolean.TRUE.equals(cleanUp);
    }

    public void setCleanUp(boolean cleanUp) {
        this.cleanUp = cleanUp;
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

    public String getDeadLetterControllerTopic() { return deadLetterControllerTopicKey; }

    public void setDeadLetterControllerTopic(String deadLetterControllerTopic) {
        this.deadLetterControllerTopicKey = deadLetterControllerTopic;
    }

    public boolean isDeadLetterEnabled() {
        return Boolean.TRUE.equals(deadLetterEnabled);
    }

    public void setDeadLetterEnabled(boolean deadLetterEnabled) {
        this.deadLetterEnabled = deadLetterEnabled;
    }

    public String getDeadLetterTopicExt() {
        return deadLetterTopicExt;
    }

    public void setDeadLetterTopicExt(String deadLetterTopicExt) {
        this.deadLetterTopicExt = deadLetterTopicExt;
    }
}
