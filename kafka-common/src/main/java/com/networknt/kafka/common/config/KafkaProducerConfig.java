package com.networknt.kafka.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.networknt.config.Config;
import com.networknt.config.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.networknt.server.ModuleRegistry;

import java.util.Map;

import static com.networknt.kafka.common.config.KafkaConfigUtils.getFromMappedConfigAsType;

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
    private KafkaProducerPropertiesConfig properties = new KafkaProducerPropertiesConfig();

    @StringField(
            configFieldName = TOPIC_KEY,
            externalizedKeyName = TOPIC_KEY,
            defaultValue = "portal-event",
            description = "The default topic for the producer. Only certain producer implementation will use it."
    )
    @JsonProperty(TOPIC_KEY)
    private String topic = "portal-event";

    @StringField(
            configFieldName = KEY_FORMAT_KEY,
            externalizedKeyName = KEY_FORMAT_KEY,
            defaultValue = "jsonschema",
            description = "Default key format if no schema for the topic value"
    )
    @JsonProperty(KEY_FORMAT_KEY)
    private String keyFormat = "jsonschema";

    @StringField(
            configFieldName = VALUE_FORMAT_KEY,
            externalizedKeyName = VALUE_FORMAT_KEY,
            defaultValue = "jsonschema",
            description = "Default value format if no schema for the topic value"
    )
    @JsonProperty(VALUE_FORMAT_KEY)
    private String valueFormat = "jsonschema";

    @BooleanField(
            configFieldName = INJECT_OPEN_TRACING_KEY,
            externalizedKeyName = INJECT_OPEN_TRACING_KEY,
            defaultValue = "false",
            description = "If open tracing is enable. traceability, correlation and metrics should not be in the chain if opentracing is used."
    )
    @JsonProperty(INJECT_OPEN_TRACING_KEY)
    private Boolean injectOpenTracing = false;

    @BooleanField(
            configFieldName = INJECT_CALLER_ID_KEY,
            externalizedKeyName = INJECT_CALLER_ID_KEY,
            defaultValue = "false",
            description = "Inject serviceId as callerId into the http header for metrics to collect the caller. The serviceId is from server.yml"
    )
    @JsonProperty(INJECT_CALLER_ID_KEY)
    private Boolean injectCallerId = false;

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

    private Map<String, Object> mappedConfig;
    private static volatile KafkaProducerConfig instance;

    public KafkaProducerConfig() {
        this(CONFIG_NAME);
    }

    public KafkaProducerConfig(final String configName) {
        this.mappedConfig = Config.getInstance().getJsonMapConfig(configName);
        this.setConfigData();
    }

    public static KafkaProducerConfig load() {
        return load(CONFIG_NAME);
    }

    public static KafkaProducerConfig load(final String configName) {
        if (CONFIG_NAME.equals(configName)) {
            Map<String, Object> mappedConfig = Config.getInstance().getJsonMapConfig(configName);
            if (instance != null && instance.getMappedConfig() == mappedConfig) {
                return instance;
            }
            synchronized (KafkaProducerConfig.class) {
                mappedConfig = Config.getInstance().getJsonMapConfig(configName);
                if (instance != null && instance.getMappedConfig() == mappedConfig) {
                    return instance;
                }
                instance = new KafkaProducerConfig(configName);
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
                instance = new KafkaProducerConfig(configName);
                ModuleRegistry.registerModule(CONFIG_NAME, KafkaProducerConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), null);
            }
        }
    }

    private void setConfigData() {
        final var mapper = Config.getInstance().getMapper();
        this.properties = getFromMappedConfigAsType(this.mappedConfig, mapper, PROPERTIES_KEY, KafkaProducerPropertiesConfig.class);
        this.topic = getFromMappedConfigAsType(this.mappedConfig,mapper, TOPIC_KEY, String.class);
        this.keyFormat = getFromMappedConfigAsType(this.mappedConfig,mapper, KEY_FORMAT_KEY, String.class);
        this.valueFormat = getFromMappedConfigAsType(this.mappedConfig,mapper, VALUE_FORMAT_KEY, String.class);
        this.injectOpenTracing = getFromMappedConfigAsType(this.mappedConfig,mapper, INJECT_OPEN_TRACING_KEY, Boolean.class);
        this.injectCallerId = getFromMappedConfigAsType(this.mappedConfig,mapper, INJECT_CALLER_ID_KEY, Boolean.class);
        this.auditEnabled = getFromMappedConfigAsType(this.mappedConfig,mapper, AUDIT_ENABLED_KEY, Boolean.class);
        this.auditTarget = getFromMappedConfigAsType(this.mappedConfig,mapper, AUDIT_TARGET_KEY, String.class);
        this.auditTopic = getFromMappedConfigAsType(this.mappedConfig,mapper, AUDIT_TOPIC_KEY, String.class);
    }

    public Map<String, Object> getMappedConfig() {
        return mappedConfig;
    }

    public KafkaProducerPropertiesConfig getProperties() {
        return properties;
    }

    public Map<String, Object> getKafkaMapProperties() {
        return properties.getMergedProperties();
    }

    public String getTopic() {
        return topic;
    }

    public String getKeyFormat() {
        return keyFormat;
    }

    public String getValueFormat() {
        return valueFormat;
    }

    public boolean isInjectOpenTracing() {
        return injectOpenTracing;
    }

    public boolean isInjectCallerId() {
        return injectCallerId;
    }

    public boolean isAuditEnabled() {
        return auditEnabled;
    }

    public String getAuditTarget() {
        return auditTarget;
    }

    public String getAuditTopic() {
        return auditTopic;
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
