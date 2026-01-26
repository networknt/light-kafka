package com.networknt.kafka.common.config;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.networknt.config.Config;
import com.networknt.config.schema.*;

import com.networknt.server.ModuleRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private KafkaStreamsPropertiesConfig properties = new KafkaStreamsPropertiesConfig();

    @BooleanField(
            configFieldName = CLEAN_UP_KEY,
            externalizedKeyName = CLEAN_UP_KEY,
            defaultValue = "false",
            description = "Only set to true right after the streams reset and start the server. Once the server is up, shutdown and change this to false and restart."
    )
    private Boolean cleanUp = false;

    @BooleanField(
            configFieldName = DEAD_LETTER_ENABLED_KEY,
            externalizedKeyName = DEAD_LETTER_ENABLED_KEY,
            defaultValue = "true",
            description = "Common configuration properties between active and reactive consumers\n" +
                    "Indicator if the dead letter topic is enabled."
    )
    @JsonProperty(DEAD_LETTER_ENABLED_KEY)
    private Boolean deadLetterEnabled = true;

    @StringField(
            configFieldName = DEAD_LETTER_TOPIC_EXT_KEY,
            externalizedKeyName = DEAD_LETTER_TOPIC_EXT_KEY,
            defaultValue = ".dlq",
            description = "The extension of the dead letter queue(topic) that is added to the original topic to form the dead letter topic"
    )
    @JsonProperty(DEAD_LETTER_TOPIC_EXT_KEY)
    private String deadLetterTopicExt = ".dlq";

    @BooleanField(
            configFieldName = AUDIT_ENABLED_KEY,
            externalizedKeyName = AUDIT_ENABLED_KEY,
            defaultValue = "true",
            description = "If audit is enabled, the producer will send the audit message to the audit topic."
    )
    @JsonProperty(AUDIT_ENABLED_KEY)
    private Boolean auditEnabled = true;

    @StringField(
            configFieldName = AUDIT_TARGET_KEY,
            externalizedKeyName = AUDIT_TARGET_KEY,
            defaultValue = "logfile",
            description = "Audit log destination topic or logfile. Default to topic"
    )
    @JsonProperty(AUDIT_TARGET_KEY)
    private String auditTarget = "logfile";

    @StringField(
            configFieldName = AUDIT_TOPIC_KEY,
            externalizedKeyName = AUDIT_TOPIC_KEY,
            defaultValue = "sidecar-audit",
            description = "The consumer audit topic name if the auditTarget is topic"
    )
    @JsonProperty(AUDIT_TOPIC_KEY)
    private String auditTopic = "sidecar-audit";

    @StringField(
            configFieldName = DEAD_LETTER_CONTROLLER_TOPIC_KEY,
            externalizedKeyName = DEAD_LETTER_CONTROLLER_TOPIC_KEY,
            defaultValue = "dev.ent.all.kafka.replay.metadata.0",
            description = "The dead letter controller topic, one per environment."
    )
    @JsonProperty(DEAD_LETTER_CONTROLLER_TOPIC_KEY)
    private String deadLetterControllerTopicKey = "dev.ent.all.kafka.replay.metadata.0";


    private final Config config;
    private Map<String, Object> mappedConfig;
    private static final Map<String, KafkaStreamsConfig> instances = new ConcurrentHashMap<>();

    public KafkaStreamsConfig() {
        this(CONFIG_NAME);
    }

    public KafkaStreamsConfig(final String configName) {
        this.config = Config.getInstance();
        this.mappedConfig = config.getJsonMapConfigNoCache(configName);
        this.setConfigData();
    }

    public static KafkaStreamsConfig load() {
        return load(CONFIG_NAME);
    }

    public static KafkaStreamsConfig load(final String configName) {
        KafkaStreamsConfig instance = instances.get(configName);
        if (instance != null) {
            return instance;
        }
        synchronized (KafkaStreamsConfig.class) {
            instance = instances.get(configName);
            if (instance != null) {
                return instance;
            }
            instance = new KafkaStreamsConfig(configName);
            instances.put(configName, instance);
            if (CONFIG_NAME.equals(configName)) {
                ModuleRegistry.registerModule(CONFIG_NAME, KafkaStreamsConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), MASKS);
            }
            return instance;
        }
    }

    public static void reload() {
        reload(CONFIG_NAME);
    }

    public static void reload(String configName) {
        synchronized (KafkaStreamsConfig.class) {
            KafkaStreamsConfig instance = new KafkaStreamsConfig(configName);
            instances.put(configName, instance);
            if (CONFIG_NAME.equals(configName)) {
                ModuleRegistry.registerModule(CONFIG_NAME, KafkaStreamsConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), MASKS);
            }
        }
    }

    private void setConfigData() {
        final var mapper = config.getMapper();
        this.properties = getFromMappedConfigAsType(this.mappedConfig, mapper, PROPERTIES_KEY, KafkaStreamsPropertiesConfig.class);
        this.cleanUp = getFromMappedConfigAsType(this.mappedConfig, mapper, CLEAN_UP_KEY, Boolean.class);
    }

    public Map<String, Object> getKafkaMapProperties() {
        return properties.getMergedProperties();
    }

    public KafkaStreamsPropertiesConfig getProperties() {
        return properties;
    }

    public Boolean getCleanUp() {
        return cleanUp;
    }

    public Boolean isAuditEnabled() {
        return auditEnabled;
    }

    public String getAuditTarget() {
        return auditTarget;
    }

    public String getAuditTopic() {
        return auditTopic;
    }

    public String getDeadLetterControllerTopic() { return deadLetterControllerTopicKey; }

    public Boolean isDeadLetterEnabled() {
        return deadLetterEnabled;
    }

    public String getDeadLetterTopicExt() {
        return deadLetterTopicExt;
    }

    //    public static final String AUDIT_TARGET_TOPIC = "topic";
//    public static final String AUDIT_TARGET_LOGFILE = "logfile";
//    private boolean auditEnabled;
//    private String auditTarget;
//    private String auditTopic;
//    private boolean deadLetterEnabled;
//    private String deadLetterTopicExt;
//    private String deadLetterControllerTopic;


//    public KafkaStreamsConfig() {
//    }
//
//    public boolean isCleanUp() {
//        return cleanUp;
//    }
//
//    public void setCleanUp(boolean cleanUp) {
//        this.cleanUp = cleanUp;
//    }
//    public boolean isAuditEnabled() {
//        return auditEnabled;
//    }
//
//    public void setAuditEnabled(boolean auditEnabled) {
//        this.auditEnabled = auditEnabled;
//    }
//
//    public String getAuditTarget() {
//        return auditTarget;
//    }
//
//    public void setAuditTarget(String auditTarget) {
//        this.auditTarget = auditTarget;
//    }
//
//    public String getAuditTopic() {
//        return auditTopic;
//    }
//
//    public void setAuditTopic(String auditTopic) {
//        this.auditTopic = auditTopic;
//    }
//
//    public boolean isDeadLetterEnabled() {
//        return deadLetterEnabled;
//    }
//
//    public void setDeadLetterEnabled(boolean deadLetterEnabled) {
//        this.deadLetterEnabled = deadLetterEnabled;
//    }
//
//    public String getDeadLetterTopicExt() {
//        return deadLetterTopicExt;
//    }
//
//    public void setDeadLetterTopicExt(String deadLetterTopicExt) {
//        this.deadLetterTopicExt = deadLetterTopicExt;
//    }
//
//    public String getDeadLetterControllerTopic() {
//        return deadLetterControllerTopic;
//    }
//
//    public void setDeadLetterControllerTopic(String deadLetterControllerTopic) {
//        this.deadLetterControllerTopic = deadLetterControllerTopic;
//    }
//
//
//    public Map<String, Object> getProperties() {
//        return properties;
//    }
//
//    public void setProperties(Map<String, Object> properties) {
//        this.properties = properties;
//    }
}
