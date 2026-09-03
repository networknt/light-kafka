package com.networknt.kafka.common;

import com.networknt.config.Config;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class KafkaConfigApiCompatibilityTest {

    @Test
    void noArgConstructorsRetain230PojoDefaults() {
        KafkaConsumerConfig consumer = new KafkaConsumerConfig();
        assertNull(consumer.getProperties());
        consumer.setProperties(null);
        assertNull(consumer.getProperties());
        assertEquals(0, consumer.getMaxConsumerThreads());
        assertEquals(0L, consumer.getRequestMaxBytes());
        assertFalse(consumer.isAuditEnabled());

        KafkaProducerConfig producer = new KafkaProducerConfig();
        assertNull(producer.getProperties());
        producer.setProperties(null);
        assertNull(producer.getProperties());
        assertNull(producer.getTopic());
        assertFalse(producer.isInjectOpenTracing());
        assertFalse(producer.isAuditEnabled());

        KafkaStreamsConfig streams = new KafkaStreamsConfig();
        assertNull(streams.getProperties());
        streams.setProperties(null);
        assertNull(streams.getProperties());
        assertFalse(streams.isCleanUp());
        assertFalse(streams.isDeadLetterEnabled());
        assertFalse(streams.isAuditEnabled());
    }

    @Test
    void legacyJsonObjectLoadingStillPopulatesMapProperties() {
        KafkaConsumerConfig consumer = (KafkaConsumerConfig) Config.getInstance()
                .getJsonObjectConfig(KafkaConsumerConfig.CONFIG_NAME, KafkaConsumerConfig.class);
        assertNotNull(consumer.getProperties().get("group.id"));
        assertEquals(consumer.getProperties().get("group.id"), consumer.getGroupId());

        KafkaProducerConfig producer = (KafkaProducerConfig) Config.getInstance()
                .getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);
        assertEquals("http://localhost:8081", producer.getProperties().get("schema.registry.url"));

        KafkaStreamsConfig streams = (KafkaStreamsConfig) Config.getInstance()
                .getJsonObjectConfig(KafkaStreamsConfig.CONFIG_NAME, KafkaStreamsConfig.class);
        assertEquals("http://localhost:8081", streams.getProperties().get("schema.registry.url"));
    }

    @Test
    void legacyJsonObjectLoadingNormalizesOpenEndedProperties() {
        KafkaConsumerConfig consumer = (KafkaConsumerConfig) Config.getInstance()
                .getJsonObjectConfigNoCache("kafka-consumer-open-ended", KafkaConsumerConfig.class);
        assertEquals("1001", consumer.getGroupId());
        assertEquals("1001", consumer.getProperties().get("group.id"));
        assertEquals("inline-consumer-value", consumer.getProperties().get("inline.custom.property"));
        assertEquals("myNewValue", consumer.getProperties().get("myNewProperty"));
        assertEquals("http://localhost:8081", consumer.getProperties().get("schema.registry.url"));
        assertFalse(consumer.getProperties().containsKey("max.poll.records"));
        assertEquals("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"12345\" password=\"67890\";",
                consumer.getProperties().get("sasl.jaas.config"));
        assertFalse(consumer.getProperties().containsKey("sasl.jaas.config.module"));
        assertFalse(consumer.getProperties().containsKey("additionalKafkaProperties"));

        KafkaProducerConfig producer = (KafkaProducerConfig) Config.getInstance()
                .getJsonObjectConfigNoCache("kafka-producer-additionalProps", KafkaProducerConfig.class);
        assertEquals("inline-producer-value", producer.getProperties().get("inline.custom.property"));
        assertEquals("myNewValue", producer.getProperties().get("myNewProperty"));
        assertEquals("http://localhost:8081", producer.getProperties().get("schema.registry.url"));
        assertFalse(producer.getProperties().containsKey("additionalKafkaProperties"));

        KafkaStreamsConfig streams = (KafkaStreamsConfig) Config.getInstance()
                .getJsonObjectConfigNoCache("kafka-streams-additionalProps", KafkaStreamsConfig.class);
        assertEquals("inline-streams-value", streams.getProperties().get("inline.custom.property"));
        assertEquals("myNewValue", streams.getProperties().get("myNewProperty"));
        assertEquals("http://localhost:8081", streams.getProperties().get("schema.registry.url"));
        assertFalse(streams.getProperties().containsKey("additionalKafkaProperties"));
    }

    @Test
    void propertySettersRetainMutableMapAliases() {
        Map<String, Object> consumerProperties = new HashMap<>();
        consumerProperties.put("group.id", 1001);
        KafkaConsumerConfig consumer = new KafkaConsumerConfig();
        consumer.setProperties(consumerProperties);
        assertSame(consumerProperties, consumer.getProperties());
        assertEquals("1001", consumer.getGroupId());
        consumerProperties.put("bootstrap.servers", "consumer:9092");
        assertEquals("consumer:9092", consumer.getProperties().get("bootstrap.servers"));

        Map<String, Object> producerProperties = new HashMap<>();
        producerProperties.put("sasl.jaas.config.module", "org.example.LoginModule");
        producerProperties.put("sasl.jaas.config.username", 12345);
        producerProperties.put("sasl.jaas.config.password", 67890);
        KafkaProducerConfig producer = new KafkaProducerConfig();
        producer.setProperties(producerProperties);
        assertSame(producerProperties, producer.getProperties());
        assertEquals("org.example.LoginModule required username=\"12345\" password=\"67890\";",
                producer.getProperties().get("sasl.jaas.config"));
        assertFalse(producer.getProperties().containsKey("sasl.jaas.config.username"));
        producerProperties.put("bootstrap.servers", "producer:9092");
        assertEquals("producer:9092", producer.getProperties().get("bootstrap.servers"));

        Map<String, Object> streamsProperties = new HashMap<>();
        KafkaStreamsConfig streams = new KafkaStreamsConfig();
        streams.setProperties(streamsProperties);
        assertSame(streamsProperties, streams.getProperties());
        streamsProperties.put("bootstrap.servers", "streams:9092");
        assertEquals("streams:9092", streams.getProperties().get("bootstrap.servers"));
    }

    @Test
    void rootSaslConfigurationTakesPrecedenceOverAdditionalProperties() {
        Map<String, Object> staleAdditionalProperties = new HashMap<>();
        staleAdditionalProperties.put("sasl.jaas.config", "STALE");

        Map<String, Object> componentProperties = new HashMap<>();
        componentProperties.put("additionalKafkaProperties", staleAdditionalProperties);
        componentProperties.put("sasl.jaas.config.module", "org.example.LoginModule");
        componentProperties.put("sasl.jaas.config.username", "newuser");
        componentProperties.put("sasl.jaas.config.password", "newpass");

        KafkaProducerConfig componentsWin = new KafkaProducerConfig();
        componentsWin.setProperties(componentProperties);
        assertEquals("org.example.LoginModule required username=\"newuser\" password=\"newpass\";",
                componentsWin.getProperties().get("sasl.jaas.config"));

        Map<String, Object> explicitProperties = new HashMap<>();
        explicitProperties.put("additionalKafkaProperties", Map.of(
                "sasl.jaas.config.module", "org.example.StaleLoginModule",
                "sasl.jaas.config.username", "staleuser",
                "sasl.jaas.config.password", "stalepass"));
        explicitProperties.put("sasl.jaas.config", "ROOT");

        KafkaProducerConfig explicitWins = new KafkaProducerConfig();
        explicitWins.setProperties(explicitProperties);
        assertEquals("ROOT", explicitWins.getProperties().get("sasl.jaas.config"));
    }

    @Test
    void loadsFlat230ConsumerConfiguration() {
        KafkaConsumerConfig consumer = KafkaConsumerConfig.load("kafka-consumer-2.3.0");

        assertEquals("group1", consumer.getGroupId());
        assertEquals("group1", consumer.getKafkaMapProperties().get("group.id"));
        assertEquals("localhost:9092", consumer.getKafkaMapProperties().get("bootstrap.servers"));
        assertEquals("read_committed", consumer.getKafkaMapProperties().get("isolation.level"));
        assertEquals(false, consumer.getKafkaMapProperties().get("enable.auto.commit"));
        assertEquals(50, consumer.getIteratorBackoffMs());
        assertEquals(30, consumer.getBatchRollbackThreshold());
    }

    @Test
    void consumerConfigPreserves230MethodDescriptors() throws Exception {
        KafkaConsumerConfig.class.getConstructor();
        assertEquals(String.class, KafkaConsumerConfig.class.getField("CONFIG_NAME").getType());
        assertEquals(String.class, KafkaConsumerConfig.class.getField("AUDIT_TARGET_TOPIC").getType());
        assertEquals(String.class, KafkaConsumerConfig.class.getField("AUDIT_TARGET_LOGFILE").getType());
        assertMethod(KafkaConsumerConfig.class, "getGroupId", String.class);
        assertMethod(KafkaConsumerConfig.class, "setGroupId", void.class, String.class);
        assertMethod(KafkaConsumerConfig.class, "getMaxConsumerThreads", int.class);
        assertMethod(KafkaConsumerConfig.class, "setMaxConsumerThreads", void.class, int.class);
        assertMethod(KafkaConsumerConfig.class, "getServerId", String.class);
        assertMethod(KafkaConsumerConfig.class, "setServerId", void.class, String.class);
        assertMethod(KafkaConsumerConfig.class, "getRequestMaxBytes", long.class);
        assertMethod(KafkaConsumerConfig.class, "setRequestMaxBytes", void.class, long.class);
        assertMethod(KafkaConsumerConfig.class, "getRequestTimeoutMs", int.class);
        assertMethod(KafkaConsumerConfig.class, "setRequestTimeoutMs", void.class, int.class);
        assertMethod(KafkaConsumerConfig.class, "getInstanceTimeoutMs", int.class);
        assertMethod(KafkaConsumerConfig.class, "setInstanceTimeoutMs", void.class, int.class);
        assertMethod(KafkaConsumerConfig.class, "getFetchMinBytes", int.class);
        assertMethod(KafkaConsumerConfig.class, "setFetchMinBytes", void.class, int.class);
        assertMethod(KafkaConsumerConfig.class, "getIteratorBackoffMs", int.class);
        assertMethod(KafkaConsumerConfig.class, "setIteratorBackoffMs", void.class, int.class);
        assertMethod(KafkaConsumerConfig.class, "getTopic", String.class);
        assertMethod(KafkaConsumerConfig.class, "setTopic", void.class, String.class);
        assertMethod(KafkaConsumerConfig.class, "getWaitPeriod", int.class);
        assertMethod(KafkaConsumerConfig.class, "setWaitPeriod", void.class, int.class);
        assertMethod(KafkaConsumerConfig.class, "getProperties", Map.class);
        assertMethod(KafkaConsumerConfig.class, "setProperties", void.class, Map.class);
        assertMethod(KafkaConsumerConfig.class, "getKeyFormat", String.class);
        assertMethod(KafkaConsumerConfig.class, "setKeyFormat", void.class, String.class);
        assertMethod(KafkaConsumerConfig.class, "getValueFormat", String.class);
        assertMethod(KafkaConsumerConfig.class, "setValueFormat", void.class, String.class);
        assertMethod(KafkaConsumerConfig.class, "getBackendApiHost", String.class);
        assertMethod(KafkaConsumerConfig.class, "setBackendApiHost", void.class, String.class);
        assertMethod(KafkaConsumerConfig.class, "getBackendApiPath", String.class);
        assertMethod(KafkaConsumerConfig.class, "setBackendApiPath", void.class, String.class);
        assertMethod(KafkaConsumerConfig.class, "isDeadLetterEnabled", boolean.class);
        assertMethod(KafkaConsumerConfig.class, "setDeadLetterEnabled", void.class, boolean.class);
        assertMethod(KafkaConsumerConfig.class, "getDeadLetterTopicExt", String.class);
        assertMethod(KafkaConsumerConfig.class, "setDeadLetterTopicExt", void.class, String.class);
        assertMethod(KafkaConsumerConfig.class, "getAuditTopic", String.class);
        assertMethod(KafkaConsumerConfig.class, "setAuditTopic", void.class, String.class);
        assertMethod(KafkaConsumerConfig.class, "isAuditEnabled", boolean.class);
        assertMethod(KafkaConsumerConfig.class, "setAuditEnabled", void.class, boolean.class);
        assertMethod(KafkaConsumerConfig.class, "getAuditTarget", String.class);
        assertMethod(KafkaConsumerConfig.class, "setAuditTarget", void.class, String.class);
        assertMethod(KafkaConsumerConfig.class, "isUseNoWrappingAvro", boolean.class);
        assertMethod(KafkaConsumerConfig.class, "setUseNoWrappingAvro", void.class, boolean.class);
        assertMethod(KafkaConsumerConfig.class, "isBackendConnectionReset", boolean.class);
        assertMethod(KafkaConsumerConfig.class, "setBackendConnectionReset", void.class, boolean.class);
        assertMethod(KafkaConsumerConfig.class, "getBatchRollbackThreshold", int.class);
        assertMethod(KafkaConsumerConfig.class, "setBatchRollbackThreshold", void.class, int.class);
    }

    @Test
    void producerConfigPreserves230MethodDescriptors() throws Exception {
        KafkaProducerConfig.class.getConstructor();
        assertEquals(String.class, KafkaProducerConfig.class.getField("CONFIG_NAME").getType());
        assertEquals(String.class, KafkaProducerConfig.class.getField("AUDIT_TARGET_TOPIC").getType());
        assertEquals(String.class, KafkaProducerConfig.class.getField("AUDIT_TARGET_LOGFILE").getType());
        assertMethod(KafkaProducerConfig.class, "getProperties", Map.class);
        assertMethod(KafkaProducerConfig.class, "setProperties", void.class, Map.class);
        assertMethod(KafkaProducerConfig.class, "getTopic", String.class);
        assertMethod(KafkaProducerConfig.class, "setTopic", void.class, String.class);
        assertMethod(KafkaProducerConfig.class, "isInjectOpenTracing", boolean.class);
        assertMethod(KafkaProducerConfig.class, "setInjectOpenTracing", void.class, boolean.class);
        assertMethod(KafkaProducerConfig.class, "isInjectCallerId", boolean.class);
        assertMethod(KafkaProducerConfig.class, "setInjectCallerId", void.class, boolean.class);
        assertMethod(KafkaProducerConfig.class, "getAuditTopic", String.class);
        assertMethod(KafkaProducerConfig.class, "setAuditTopic", void.class, String.class);
        assertMethod(KafkaProducerConfig.class, "isAuditEnabled", boolean.class);
        assertMethod(KafkaProducerConfig.class, "setAuditEnabled", void.class, boolean.class);
        assertMethod(KafkaProducerConfig.class, "getAuditTarget", String.class);
        assertMethod(KafkaProducerConfig.class, "setAuditTarget", void.class, String.class);
        assertMethod(KafkaProducerConfig.class, "getKeyFormat", String.class);
        assertMethod(KafkaProducerConfig.class, "setKeyFormat", void.class, String.class);
        assertMethod(KafkaProducerConfig.class, "getValueFormat", String.class);
        assertMethod(KafkaProducerConfig.class, "setValueFormat", void.class, String.class);
    }

    @Test
    void streamsConfigPreserves230MethodDescriptors() throws Exception {
        KafkaStreamsConfig.class.getConstructor();
        assertEquals(String.class, KafkaStreamsConfig.class.getField("CONFIG_NAME").getType());
        assertEquals(String.class, KafkaStreamsConfig.class.getField("AUDIT_TARGET_TOPIC").getType());
        assertEquals(String.class, KafkaStreamsConfig.class.getField("AUDIT_TARGET_LOGFILE").getType());
        assertMethod(KafkaStreamsConfig.class, "isCleanUp", boolean.class);
        assertMethod(KafkaStreamsConfig.class, "setCleanUp", void.class, boolean.class);
        assertMethod(KafkaStreamsConfig.class, "isAuditEnabled", boolean.class);
        assertMethod(KafkaStreamsConfig.class, "setAuditEnabled", void.class, boolean.class);
        assertMethod(KafkaStreamsConfig.class, "getAuditTarget", String.class);
        assertMethod(KafkaStreamsConfig.class, "setAuditTarget", void.class, String.class);
        assertMethod(KafkaStreamsConfig.class, "getAuditTopic", String.class);
        assertMethod(KafkaStreamsConfig.class, "setAuditTopic", void.class, String.class);
        assertMethod(KafkaStreamsConfig.class, "isDeadLetterEnabled", boolean.class);
        assertMethod(KafkaStreamsConfig.class, "setDeadLetterEnabled", void.class, boolean.class);
        assertMethod(KafkaStreamsConfig.class, "getDeadLetterTopicExt", String.class);
        assertMethod(KafkaStreamsConfig.class, "setDeadLetterTopicExt", void.class, String.class);
        assertMethod(KafkaStreamsConfig.class, "getDeadLetterControllerTopic", String.class);
        assertMethod(KafkaStreamsConfig.class, "setDeadLetterControllerTopic", void.class, String.class);
        assertMethod(KafkaStreamsConfig.class, "getProperties", Map.class);
        assertMethod(KafkaStreamsConfig.class, "setProperties", void.class, Map.class);
    }

    private static void assertMethod(Class<?> type, String name, Class<?> returnType, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = type.getMethod(name, parameterTypes);
        assertEquals(returnType, method.getReturnType(), method.toString());
    }
}
