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
    public static final String BATCH_ROLLBACK_THRESHOLD_KEY = "batchRollbackThreshold";

    @ObjectField(
            configFieldName = PROPERTIES_KEY,
            useSubObjectDefault = true,
            description = "Generic Kafka Consumer Configuration",
            ref = KafkaConsumerPropertiesConfig.class
    )
    @JsonProperty(PROPERTIES_KEY)
    // This typed field is used by schema generation. Runtime loading retains the raw,
    // open-ended Kafka property map required by the 2.3.0-compatible accessors.
    private KafkaConsumerPropertiesConfig propertiesConfig = new KafkaConsumerPropertiesConfig();

    private Map<String, Object> properties;
    private String groupId;

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
    private Integer iteratorBackoffMs;

    @BooleanField(
            configFieldName = BACKEND_CONNECTION_RESET_KEY,
            externalizedKeyName = BACKEND_CONNECTION_RESET_KEY,
            defaultValue = "false",
            description = "In case of .NET application we realized , under load, response comes back for batch HTTP request however FinACK does not come until\n" +
                    "keep alive time out occurs and sidecar consumer does not move forward. Hence we are adding this property so that we can explicitly close the connection\n" +
                    "when we receive the response and not wait for FinAck."
    )
    @JsonProperty(BACKEND_CONNECTION_RESET_KEY)
    private Boolean backendConnectionReset;

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

    @NumberField(
            configFieldName = BATCH_ROLLBACK_THRESHOLD_KEY,
            externalizedKeyName = BATCH_ROLLBACK_THRESHOLD_KEY,
            defaultValue = "30",
            description = "The percentage threshold (0–100) of records in a batch that are allowed to fail before the entire batch is rolled back.\n" +
                    "  - 0  = strict behavior: any failure rolls back the whole batch (nothing goes to DLQ for that batch).\n" +
                    "  - 100 = permissive behavior: the entire batch can fail and be sent to the DLQ without rollback.\n" +
                    "The default of 30 is a conservative compromise: up to 30% of records may be redirected to the DLQ\n" +
                    "while still committing the successful records, which limits reprocessing of good messages but avoids\n" +
                    "silently accepting mostly-bad batches. Adjust this value based on your tolerance for partial failures:\n" +
                    "  - Lower values (e.g. 5–10) for highly critical data where most failures should block the batch.\n" +
                    "  - Higher values (e.g. 50–80) for noisy or less critical topics where partial DLQing is acceptable.\n" +
                    "NOTE: Values outside 0–100 are considered invalid and may be rejected by the application or lead to undefined behavior.\n"
    )
    @JsonProperty(BATCH_ROLLBACK_THRESHOLD_KEY)
    private Integer batchRollbackThreshold;


    private Map<String, Object> mappedConfig;
    private static volatile KafkaConsumerConfig instance;

    public KafkaConsumerConfig() {
    }

    public KafkaConsumerConfig(final String configName) {
        this(configName, getJsonMapConfig(configName, null));
    }

    private KafkaConsumerConfig(final String configName, final Map<String, Object> mappedConfig) {
        this.deadLetterEnabled = true;
        this.deadLetterTopicExt = ".dlq";
        this.auditEnabled = true;
        this.auditTarget = "";
        this.auditTopic = "logfile";
        this.useNoWrappingAvro = false;
        this.topic = "test1";
        this.keyFormat = "jsonschema";
        this.valueFormat = "jsonschema";
        this.waitPeriod = 100;
        this.backendApiHost = "https://localhost:8444";
        this.backendApiPath = "/kafka/records";
        this.maxConsumerThreads = 50;
        this.serverId = "id";
        this.requestMaxBytes = 102400L;
        this.requestTimeoutMs = 1000;
        this.fetchMinBytes = -1;
        this.instanceTimeoutMs = 300000;
        this.iteratorBackoffMs = 50;
        this.backendConnectionReset = false;
        this.maxRetries = 3;
        this.retryDelayMs = 1000;
        this.batchRollbackThreshold = 30;
        this.mappedConfig = mappedConfig;
        this.setConfigData();
    }

    public static KafkaConsumerConfig load() {
        return load(CONFIG_NAME);
    }

    public static KafkaConsumerConfig load(final String configName) {
        if (CONFIG_NAME.equals(configName)) {
            Map<String, Object> mappedConfig = getJsonMapConfig(configName,
                    instance == null ? null : instance.getMappedConfig());
            if (instance != null && sameMappedConfig(instance.getMappedConfig(), mappedConfig)) {
                return instance;
            }
            synchronized (KafkaConsumerConfig.class) {
                if (instance != null && sameMappedConfig(instance.getMappedConfig(), mappedConfig)) {
                    return instance;
                }
                instance = new KafkaConsumerConfig(configName, mappedConfig);
                ModuleRegistry.registerModule(CONFIG_NAME, KafkaConsumerConfig.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(CONFIG_NAME), MASKS);
                return instance;
            }
        }
        return new KafkaConsumerConfig(configName);
    }

    private void setConfigData() {
        if (this.mappedConfig.containsKey(PROPERTIES_KEY)) {
            this.properties = normalizeKafkaProperties(this.mappedConfig.get(PROPERTIES_KEY));
        } else {
            this.propertiesConfig = null;
            this.properties = getLegacyKafkaProperties(this.mappedConfig);
        }
        this.groupId = this.mappedConfig.containsKey("groupId")
                ? (String) this.mappedConfig.get("groupId") : (String) this.properties.get("group.id");
        if (this.groupId != null) {
            this.properties.put("group.id", this.groupId);
        }
        this.deadLetterEnabled = getIfPresent(DEAD_LETTER_ENABLED_KEY, Boolean.class, this.deadLetterEnabled);
        this.deadLetterTopicExt = getIfPresent(DEAD_LETTER_TOPIC_EXT_KEY, String.class, this.deadLetterTopicExt);
        this.auditEnabled = getIfPresent(AUDIT_ENABLED_KEY, Boolean.class, this.auditEnabled);
        this.auditTarget = getIfPresent(AUDIT_TARGET_KEY, String.class, this.auditTarget);
        this.auditTopic = getIfPresent(AUDIT_TOPIC_KEY, String.class, this.auditTopic);
        this.useNoWrappingAvro = getIfPresent(USE_NO_WRAPPING_AVRO_KEY, Boolean.class, this.useNoWrappingAvro);
        this.topic = getIfPresent(TOPIC_KEY, String.class, this.topic);
        this.keyFormat = getIfPresent(KEY_FORMAT_KEY, String.class, this.keyFormat);
        this.valueFormat = getIfPresent(VALUE_FORMAT_KEY, String.class, this.valueFormat);
        this.waitPeriod = getIfPresent(WAIT_PERIOD_KEY, Integer.class, this.waitPeriod);
        this.backendApiHost = getIfPresent(BACKEND_API_HOST_KEY, String.class, this.backendApiHost);
        this.backendApiPath = getIfPresent(BACKEND_API_PATH_KEY, String.class, this.backendApiPath);
        this.maxConsumerThreads = getIfPresent(MAX_CONSUMER_THREADS_KEY, Integer.class, this.maxConsumerThreads);
        this.serverId = getIfPresent(SERVER_ID_KEY, String.class, this.serverId);
        this.requestMaxBytes = getIfPresent(REQUEST_MAX_BYTES_KEY, Long.class, this.requestMaxBytes);
        this.requestTimeoutMs = getIfPresent(REQUEST_TIMEOUT_MS_KEY, Integer.class, this.requestTimeoutMs);
        this.fetchMinBytes = getIfPresent(FETCH_MIN_BYTES_KEY, Integer.class, this.fetchMinBytes);
        this.instanceTimeoutMs = getIfPresent(INSTANCE_TIMEOUT_MS_KEY, Integer.class, this.instanceTimeoutMs);
        this.iteratorBackoffMs = getIfPresent(ITERATOR_BACKOFF_MS_KEY, Integer.class, this.iteratorBackoffMs);
        this.backendConnectionReset = getIfPresent(BACKEND_CONNECTION_RESET_KEY, Boolean.class, this.backendConnectionReset);
        this.maxRetries = getIfPresent(MAX_RETRIES_KEY, Integer.class, this.maxRetries);
        this.retryDelayMs = getIfPresent(RETRY_DELAY_MS_KEY, Integer.class, this.retryDelayMs);
        this.batchRollbackThreshold = getIfPresent(BATCH_ROLLBACK_THRESHOLD_KEY, Integer.class, this.batchRollbackThreshold);
    }

    private <T> T getIfPresent(final String key, final Class<T> type, final T defaultValue) {
        return this.mappedConfig.containsKey(key)
                ? getFromMappedConfigAsType(this.mappedConfig, Config.getInstance().getMapper(), key, type)
                : defaultValue;
    }

    private static Map<String, Object> getLegacyKafkaProperties(final Map<String, Object> mappedConfig) {
        Map<String, Object> properties = new HashMap<>();
        copyProperty(mappedConfig, properties, "bootstrapServers", "bootstrap.servers");
        copyProperty(mappedConfig, properties, "isolationLevel", "isolation.level");
        copyProperty(mappedConfig, properties, "enableAutoCommit", "enable.auto.commit");
        copyProperty(mappedConfig, properties, "autoCommitIntervalMs", "auto.commit.interval.ms");
        copyProperty(mappedConfig, properties, "autoOffsetReset", "auto.offset.reset");
        copyProperty(mappedConfig, properties, "keyDeserializer", "key.deserializer");
        copyProperty(mappedConfig, properties, "valueDeserializer", "value.deserializer");
        copyProperty(mappedConfig, properties, "groupId", "group.id");
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
        this.groupId = this.properties == null ? null : (String) this.properties.get("group.id");
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Boolean getDeadLetterEnabled() {
        return deadLetterEnabled;
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

    public Boolean getAuditEnabled() {
        return auditEnabled;
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

    public Boolean getUseNoWrappingAvro() {
        return useNoWrappingAvro;
    }

    public boolean isUseNoWrappingAvro() {
        return Boolean.TRUE.equals(useNoWrappingAvro);
    }

    public void setUseNoWrappingAvro(boolean useNoWrappingAvro) {
        this.useNoWrappingAvro = useNoWrappingAvro;
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

    public int getWaitPeriod() {
        return waitPeriod == null ? 0 : waitPeriod;
    }

    public void setWaitPeriod(int waitPeriod) {
        this.waitPeriod = waitPeriod;
    }

    public String getBackendApiHost() {
        return backendApiHost;
    }

    public void setBackendApiHost(String backendApiHost) {
        this.backendApiHost = backendApiHost;
    }

    public String getBackendApiPath() {
        return backendApiPath;
    }

    public void setBackendApiPath(String backendApiPath) {
        this.backendApiPath = backendApiPath;
    }

    public int getMaxConsumerThreads() {
        return maxConsumerThreads == null ? 0 : maxConsumerThreads;
    }

    public void setMaxConsumerThreads(int maxConsumerThreads) {
        this.maxConsumerThreads = maxConsumerThreads;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public long getRequestMaxBytes() {
        return requestMaxBytes == null ? 0L : requestMaxBytes;
    }

    public void setRequestMaxBytes(long requestMaxBytes) {
        this.requestMaxBytes = requestMaxBytes;
    }

    public int getRequestTimeoutMs() {
        return requestTimeoutMs == null ? 0 : requestTimeoutMs;
    }

    public void setRequestTimeoutMs(int requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public int getFetchMinBytes() {
        return fetchMinBytes == null ? 0 : fetchMinBytes;
    }

    public void setFetchMinBytes(int fetchMinBytes) {
        this.fetchMinBytes = fetchMinBytes;
    }

    public int getInstanceTimeoutMs() {
        return instanceTimeoutMs == null ? 0 : instanceTimeoutMs;
    }

    public void setInstanceTimeoutMs(int instanceTimeoutMs) {
        this.instanceTimeoutMs = instanceTimeoutMs;
    }

    public int getIteratorBackoffMs() {
        return iteratorBackoffMs == null ? 0 : iteratorBackoffMs;
    }

    public void setIteratorBackoffMs(int iteratorBackoffMs) {
        this.iteratorBackoffMs = iteratorBackoffMs;
    }

    public Boolean getBackendConnectionReset() {
        return backendConnectionReset;
    }

    public boolean isBackendConnectionReset() {
        return Boolean.TRUE.equals(backendConnectionReset);
    }

    public void setBackendConnectionReset(boolean backendConnectionReset) {
        this.backendConnectionReset = backendConnectionReset;
    }

    public Integer getMaxRetries() { return maxRetries; }

    public Integer getRetryDelayMs() { return retryDelayMs; }
    public int getBatchRollbackThreshold() { return batchRollbackThreshold == null ? 0 : batchRollbackThreshold; }

    public void setBatchRollbackThreshold(int batchRollbackThreshold) {
        this.batchRollbackThreshold = batchRollbackThreshold;
    }
}
