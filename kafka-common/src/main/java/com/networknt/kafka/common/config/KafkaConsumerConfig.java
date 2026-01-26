package com.networknt.kafka.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.networknt.config.Config;
import com.networknt.config.schema.*;

import com.networknt.server.ModuleRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.networknt.kafka.common.config.KafkaConfigUtils.getFromMappedConfigAsType;

@ConfigSchema(
        configKey = "kafka-consumer",
        configName = "kafka-consumer",
        outputFormats = {
                OutputFormat.JSON_SCHEMA,
                OutputFormat.YAML
        }
)
public class KafkaConsumerConfig {

    public static final String CONFIG_NAME = "kafka-consumer";
    private static final List<String> MASKS = Arrays.asList("basic.auth.user.info", "sasl.jaas.config", "schema.registry.ssl.truststore.password");
    public static final String AUDIT_TARGET_TOPIC = "topic";
    public static final String AUDIT_TARGET_LOGFILE = "logfile";
    public static final String PROPERTIES_KEY = "properties";
    public static final String DEAD_LETTER_ENABLED_KEY = "deadLetterEnabled";
    public static final String DEAD_LETTER_TOPIC_EXT_KEY = "deadLetterTopicExt";
    public static final String AUDIT_ENABLED_KEY = "auditEnabled";
    public static final String AUDIT_TARGET_KEY = "auditTarget";
    public static final String AUDIT_TOPIC_KEY = "auditTopic";
    public static final String USE_NO_WRAPPING_AVRO_KEY = "useNoWrappingAvro";
    public static final String TOPIC_KEY = "topic";
    public static final String KEY_FORMAT_KEY = "keyFormat";
    public static final String VALUE_FORMAT_KEY = "valueFormat";
    public static final String WAIT_PERIOD_KEY = "waitPeriod";
    public static final String BACKEND_API_HOST_KEY = "backendApiHost";
    public static final String BACKEND_API_PATH_KEY = "backendApiPath";
    public static final String MAX_CONSUMER_THREADS_KEY = "maxConsumerThreads";
    public static final String SERVER_ID_KEY = "serverId";
    public static final String REQUEST_MAX_BYTES_KEY = "requestMaxBytes";
    public static final String REQUEST_TIMEOUT_MS_KEY = "requestTimeoutMs";
    public static final String FETCH_MIN_BYTES_KEY = "fetchMinBytes";
    public static final String INSTANCE_TIMEOUT_MS_KEY = "instanceTimeoutMs";
    public static final String ITERATOR_BACKOFF_MS_KEY = "iteratorBackoffMs";
    public static final String BACKEND_CONNECTION_RESET_KEY = "backendConnectionReset";
    public static final String MAX_RETRIES_KEY = "maxRetries";
    public static final String RETRY_DELAY_MS_KEY = "retryDelayMs";

    @ObjectField(
            configFieldName = PROPERTIES_KEY,
            useSubObjectDefault = true,
            description = "Generic Kafka Consumer Configuration",
            ref = KafkaConsumerPropertiesConfig.class
    )
    @JsonProperty(PROPERTIES_KEY)
    private KafkaConsumerPropertiesConfig properties = new KafkaConsumerPropertiesConfig();

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
            description = "Indicator if the audit is enabled."
    )
    @JsonProperty(AUDIT_ENABLED_KEY)
    private Boolean auditEnabled;

    @StringField(
            configFieldName = AUDIT_TARGET_KEY,
            externalizedKeyName = AUDIT_TARGET_KEY,
            defaultValue = "",
            description = "Audit log destination topic or logfile. Default to topic"
    )
    @JsonProperty(AUDIT_TARGET_KEY)
    private String auditTarget;

    @StringField(
            configFieldName = AUDIT_TOPIC_KEY,
            externalizedKeyName = AUDIT_TOPIC_KEY,
            defaultValue = "logfile",
            description = "The consumer audit topic name if the auditTarget is topic"
    )
    @JsonProperty(AUDIT_TOPIC_KEY)
    private String auditTopic;

    @BooleanField(
            configFieldName = USE_NO_WRAPPING_AVRO_KEY,
            externalizedKeyName = USE_NO_WRAPPING_AVRO_KEY,
            defaultValue = "false",
            description = "Indicate if the NoWrapping Avro converter is used. This should be used for avro schema with data type in JSON."
    )
    @JsonProperty(USE_NO_WRAPPING_AVRO_KEY)
    private Boolean useNoWrappingAvro;

    @StringField(
            configFieldName = TOPIC_KEY,
            externalizedKeyName = TOPIC_KEY,
            defaultValue = "test1",
            description = "Reactive Consumer Specific Configuration\n" +
                    "The topic that is going to be consumed. For reactive consumer only in the kafka-sidecar.\n" +
                    "If two or more topics are going to be subscribed, concat them with comma without space.\n" +
                    "topic: sidecar-test"
    )
    @JsonProperty(TOPIC_KEY)
    private String topic;

    @StringField(
            configFieldName = KEY_FORMAT_KEY,
            externalizedKeyName = KEY_FORMAT_KEY,
            defaultValue = "jsonschema",
            description = "the format of the key optional"
    )
    @JsonProperty(KEY_FORMAT_KEY)
    private String keyFormat;

    @StringField(
            configFieldName = VALUE_FORMAT_KEY,
            externalizedKeyName = VALUE_FORMAT_KEY,
            defaultValue = "jsonschema",
            description = "the format of the value optional"
    )
    @JsonProperty(VALUE_FORMAT_KEY)
    private String valueFormat;

    @StringField(
            configFieldName = WAIT_PERIOD_KEY,
            externalizedKeyName = WAIT_PERIOD_KEY,
            defaultValue = "100",
            description = "Waiting period in millisecond to poll another batch"
    )
    @JsonProperty(WAIT_PERIOD_KEY)
    private Integer waitPeriod;

    @StringField(
            configFieldName = BACKEND_API_HOST_KEY,
            externalizedKeyName = BACKEND_API_HOST_KEY,
            defaultValue = "https://localhost:8444",
            description = "Backend API host"
    )
    @JsonProperty(BACKEND_API_HOST_KEY)
    private String backendApiHost;

    @StringField(
            configFieldName = BACKEND_API_PATH_KEY,
            externalizedKeyName = BACKEND_API_PATH_KEY,
            defaultValue = "/kafka/records",
            description = "Backend API path"
    )
    @JsonProperty(BACKEND_API_PATH_KEY)
    private String backendApiPath;

    @NumberField(
            configFieldName = MAX_CONSUMER_THREADS_KEY,
            externalizedKeyName = MAX_CONSUMER_THREADS_KEY,
            defaultValue = "50",
            description = "Active Consumer Specific Configuration and the reactive consumer also depends on these properties\n" +
                    "default max consumer threads to 50."
    )
    @JsonProperty(MAX_CONSUMER_THREADS_KEY)
    private Integer maxConsumerThreads;

    @StringField(
            configFieldName = SERVER_ID_KEY,
            externalizedKeyName = SERVER_ID_KEY,
            defaultValue = "id",
            description = "a unique id for the server instance, if running in a Kubernetes cluster, use the container id environment variable"
    )
    @JsonProperty(SERVER_ID_KEY)
    private String serverId;

    @NumberField(
            configFieldName = REQUEST_MAX_BYTES_KEY,
            externalizedKeyName = REQUEST_MAX_BYTES_KEY,
            defaultValue = "102400",
            description = "maximum number of bytes message keys and values returned. Default to 100*1024"
    )
    @JsonProperty(REQUEST_MAX_BYTES_KEY)
    private Long requestMaxBytes;

    @NumberField(
            configFieldName = REQUEST_TIMEOUT_MS_KEY,
            externalizedKeyName = REQUEST_TIMEOUT_MS_KEY,
            defaultValue = "1000",
            description = "The maximum total time to wait for messages for a request if the maximum number of messages hs not yet been reached."
    )
    @JsonProperty(REQUEST_TIMEOUT_MS_KEY)
    private Integer requestTimeoutMs;

    @NumberField(
            configFieldName = FETCH_MIN_BYTES_KEY,
            externalizedKeyName = FETCH_MIN_BYTES_KEY,
            defaultValue = "-1",
            description = "Minimum bytes of records to accumulate before returning a response to a consumer request. Default 10MB"
    )
    @JsonProperty(FETCH_MIN_BYTES_KEY)
    private Integer fetchMinBytes;

    @NumberField(
            configFieldName = INSTANCE_TIMEOUT_MS_KEY,
            externalizedKeyName = INSTANCE_TIMEOUT_MS_KEY,
            defaultValue = "300000",
            description = "amount of idle time before a consumer instance is automatically destroyed. If you use the ActiveConsumer and do not\n" +
                    "want to recreate the consumer instance for every request, increase this number to a bigger value. Default to 5 minutes\n" +
                    "that is in sync with max.poll.interval.ms default value. When this value is increased to a value greater than 5 minutes,\n" +
                    "the max.poll.interval.ms will be automatically increased as these two values are related although completely different."
    )
    @JsonProperty(INSTANCE_TIMEOUT_MS_KEY)
    private Integer instanceTimeoutMs;

    @NumberField(
            configFieldName = ITERATOR_BACKOFF_MS_KEY,
            externalizedKeyName = ITERATOR_BACKOFF_MS_KEY,
            defaultValue = "50",
            description = "Amount of time to backoff when an iterator runs out of date."
    )
    @JsonProperty(ITERATOR_BACKOFF_MS_KEY)
    private Integer iteratorBackoffMs = 50;

    @BooleanField(
            configFieldName = BACKEND_CONNECTION_RESET_KEY,
            externalizedKeyName = BACKEND_CONNECTION_RESET_KEY,
            defaultValue = "false",
            description = "In case of .NET application we realized , under load, response comes back for batch HTTP request however FinACK does not come until\n" +
                    "keep alive time out occurs and sidecar consumer does not move forward. Hence we are adding this property so that we can explicitly close the connection\n" +
                    "when we receive the response and not wait for FinAck."
    )
    @JsonProperty(BACKEND_CONNECTION_RESET_KEY)
    private Boolean backendConnectionReset = false;

    @NumberField(
            configFieldName = MAX_RETRIES_KEY,
            externalizedKeyName = MAX_RETRIES_KEY,
            defaultValue = "3",
            description = "Max retries when exception occurs."
    )
    @JsonProperty(MAX_RETRIES_KEY)
    private Integer maxRetries = 3;

    @NumberField(
            configFieldName = RETRY_DELAY_MS_KEY,
            externalizedKeyName = RETRY_DELAY_MS_KEY,
            defaultValue = "1000",
            description = "Delay milliseconds between retries."
    )
    @JsonProperty(RETRY_DELAY_MS_KEY)
    private Integer retryDelayMs = 1000;


    private Map<String, Object> mappedConfig;
    private static KafkaConsumerConfig instance;
    private static final Map<String, KafkaConsumerConfig> instances = new ConcurrentHashMap<>();

    public KafkaConsumerConfig() {
        this(CONFIG_NAME);
    }

    public KafkaConsumerConfig(final String configName) {
        this.mappedConfig = Config.getInstance().getJsonMapConfig(configName);
        this.setConfigData();
    }

    public static KafkaConsumerConfig load() {
        return load(CONFIG_NAME);
    }

    public static KafkaConsumerConfig load(final String configName) {
        if (CONFIG_NAME.equals(configName)) {
            Map<String, Object> mappedConfig = Config.getInstance().getJsonMapConfig(configName);
            if (instance != null && instance.getMappedConfig() == mappedConfig) {
                return instance;
            }
            synchronized (KafkaConsumerConfig.class) {
                mappedConfig = Config.getInstance().getJsonMapConfig(configName);
                if (instance != null && instance.getMappedConfig() == mappedConfig) {
                    return instance;
                }
                instance = new KafkaConsumerConfig(configName);
                instances.put(configName, instance);
                ModuleRegistry.registerModule(CONFIG_NAME, KafkaConsumerConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), MASKS);
                return instance;
            }
        }
        return new KafkaConsumerConfig(configName);
    }

    public static void reload() {
        reload(CONFIG_NAME);
    }

    public static void reload(String configName) {
        synchronized (KafkaConsumerConfig.class) {
            KafkaConsumerConfig instance = new KafkaConsumerConfig(configName);
            instances.put(configName, instance);
            if (CONFIG_NAME.equals(configName)) {
                ModuleRegistry.registerModule(CONFIG_NAME, KafkaConsumerConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), MASKS);
            }
        }
    }

    private void setConfigData() {
        final var mapper = Config.getInstance().getMapper();
        this.properties = getFromMappedConfigAsType(this.mappedConfig, mapper, PROPERTIES_KEY, KafkaConsumerPropertiesConfig.class);
        this.deadLetterEnabled = getFromMappedConfigAsType(this.mappedConfig, mapper, DEAD_LETTER_ENABLED_KEY, Boolean.class);
        this.deadLetterTopicExt = getFromMappedConfigAsType(this.mappedConfig, mapper, DEAD_LETTER_TOPIC_EXT_KEY, String.class);
        this.auditEnabled = getFromMappedConfigAsType(this.mappedConfig, mapper, AUDIT_ENABLED_KEY, Boolean.class);
        this.auditTarget = getFromMappedConfigAsType(this.mappedConfig, mapper, AUDIT_TARGET_KEY, String.class);
        this.auditTopic = getFromMappedConfigAsType(this.mappedConfig, mapper, AUDIT_TOPIC_KEY, String.class);
        this.useNoWrappingAvro = getFromMappedConfigAsType(this.mappedConfig, mapper, USE_NO_WRAPPING_AVRO_KEY, Boolean.class);
        this.topic = getFromMappedConfigAsType(this.mappedConfig, mapper, TOPIC_KEY, String.class);
        this.keyFormat = getFromMappedConfigAsType(this.mappedConfig, mapper, KEY_FORMAT_KEY, String.class);
        this.valueFormat = getFromMappedConfigAsType(this.mappedConfig, mapper, VALUE_FORMAT_KEY, String.class);
        this.waitPeriod = getFromMappedConfigAsType(this.mappedConfig, mapper, WAIT_PERIOD_KEY, Integer.class);
        this.backendApiHost = getFromMappedConfigAsType(this.mappedConfig, mapper, BACKEND_API_HOST_KEY, String.class);
        this.backendApiPath = getFromMappedConfigAsType(this.mappedConfig, mapper, BACKEND_API_PATH_KEY, String.class);
        this.maxConsumerThreads = getFromMappedConfigAsType(this.mappedConfig, mapper, MAX_CONSUMER_THREADS_KEY, Integer.class);
        this.serverId = getFromMappedConfigAsType(this.mappedConfig, mapper, SERVER_ID_KEY, String.class);
        this.requestMaxBytes = getFromMappedConfigAsType(this.mappedConfig, mapper, REQUEST_MAX_BYTES_KEY, Long.class);
        this.requestTimeoutMs = getFromMappedConfigAsType(this.mappedConfig, mapper, REQUEST_TIMEOUT_MS_KEY, Integer.class);
        this.fetchMinBytes = getFromMappedConfigAsType(this.mappedConfig, mapper, FETCH_MIN_BYTES_KEY, Integer.class);
        this.instanceTimeoutMs = getFromMappedConfigAsType(this.mappedConfig, mapper, INSTANCE_TIMEOUT_MS_KEY, Integer.class);
        this.iteratorBackoffMs = getFromMappedConfigAsType(this.mappedConfig, mapper, ITERATOR_BACKOFF_MS_KEY, Integer.class);
        this.backendConnectionReset = getFromMappedConfigAsType(this.mappedConfig, mapper, BACKEND_CONNECTION_RESET_KEY, Boolean.class);
        this.maxRetries = getFromMappedConfigAsType(this.mappedConfig, mapper, MAX_RETRIES_KEY, Integer.class);
        this.retryDelayMs = getFromMappedConfigAsType(this.mappedConfig, mapper, RETRY_DELAY_MS_KEY, Integer.class);
    }

    public Map<String, Object> getMappedConfig() {
        return mappedConfig;
    }

    public Map<String, Object> getKafkaMapProperties() {
        return properties.getMergedProperties();
    }

    public KafkaConsumerPropertiesConfig getProperties() {
        return properties;
    }

    public Boolean getDeadLetterEnabled() {
        return deadLetterEnabled;
    }

    public String getDeadLetterTopicExt() {
        return deadLetterTopicExt;
    }

    public Boolean getAuditEnabled() {
        return auditEnabled;
    }

    public String getAuditTarget() {
        return auditTarget;
    }

    public String getAuditTopic() {
        return auditTopic;
    }

    public Boolean getUseNoWrappingAvro() {
        return useNoWrappingAvro;
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

    public Integer getWaitPeriod() {
        return waitPeriod;
    }

    public String getBackendApiHost() {
        return backendApiHost;
    }

    public String getBackendApiPath() {
        return backendApiPath;
    }

    public Integer getMaxConsumerThreads() {
        return maxConsumerThreads;
    }

    public String getServerId() {
        return serverId;
    }

    public Long getRequestMaxBytes() {
        return requestMaxBytes;
    }

    public Integer getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public Integer getFetchMinBytes() {
        return fetchMinBytes;
    }

    public Integer getInstanceTimeoutMs() {
        return instanceTimeoutMs;
    }

    public Integer getIteratorBackoffMs() {
        return iteratorBackoffMs;
    }

    public Boolean getBackendConnectionReset() {
        return backendConnectionReset;
    }

    public Integer getMaxRetries() { return maxRetries; }

    public Integer getRetryDelayMs() { return retryDelayMs; }
}
