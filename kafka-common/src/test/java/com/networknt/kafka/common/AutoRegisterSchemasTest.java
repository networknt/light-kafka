package com.networknt.kafka.common;

import com.networknt.config.Config;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AutoRegisterSchemasTest {
    private static final String KEY = "auto.register.schemas";
    private static final String LEGACY = "schema.registry.auto.register.schemas";
    private static final Set<String> CONFIG_NAMES = Set.of("kafka-producer", "kafka-consumer", "kafka-streams");

    // Expected behavior is test data, never calculated with the production resolver.
    private record Scenario(Map<String, Object> settings, boolean enabled) { }

    private static List<Scenario> scenarios() {
        return List.of(
                new Scenario(Map.of(KEY, false), false),
                new Scenario(Map.of(LEGACY, false), false),
                new Scenario(Map.of(KEY, "false"), false),
                new Scenario(Map.of(LEGACY, "false"), false),
                new Scenario(Map.of(KEY, false, LEGACY, true), false),
                new Scenario(Map.of(KEY, true, LEGACY, false), true),
                new Scenario(Map.of(KEY, " TRUE ", LEGACY, "false"), true),
                new Scenario(Map.of(LEGACY, "TrUe"), true),
                new Scenario(Map.of(), true));
    }

    @Test
    void allClientsNormalizeBothSpellingsAndPreserveOtherProperties() {
        for (Scenario scenario : scenarios()) {
            Map<String, Object> source = new HashMap<>(scenario.settings());
            source.put("custom.setting", "preserved");
            for (Map<String, Object> result : clientProperties(source)) {
                if (!scenario.settings().isEmpty()) {
                    assertEquals(String.valueOf(scenario.enabled()),
                            String.valueOf(result.get(KEY)).trim().toLowerCase(Locale.ROOT));
                } else {
                    assertFalse(result.containsKey(KEY));
                }
                assertFalse(result.containsKey(LEGACY));
                assertEquals("preserved", result.get("custom.setting"));
            }
        }
        for (Map<String, Object> input : List.<Map<String, Object>>of(
                Map.of("additionalKafkaProperties", Map.of(LEGACY, false)),
                Map.of(LEGACY, true, "additionalKafkaProperties", Map.of(KEY, false)))) {
            for (Map<String, Object> result : clientProperties(input)) {
                assertEquals(false, result.get(KEY));
                assertFalse(result.containsKey(LEGACY));
            }
        }
    }

    @Test
    void typedModelsKeepLegacyAccessorsAndCanonicalPrecedenceInEitherOrder() throws Exception {
        for (Class<?> type : List.of(KafkaProducerPropertiesConfig.class,
                KafkaConsumerPropertiesConfig.class, KafkaStreamsPropertiesConfig.class)) {
            for (boolean canonicalFirst : new boolean[]{false, true}) {
                Map<String, Object> input = new LinkedHashMap<>();
                input.put(canonicalFirst ? KEY : LEGACY, canonicalFirst ? false : true);
                input.put(canonicalFirst ? LEGACY : KEY, canonicalFirst ? true : false);
                Object model = Config.getInstance().getMapper().convertValue(input, type);
                assertEquals(false, type.getMethod("getSchemaRegistryAutoRegisterSchemas").invoke(model));
                Map<?, ?> merged = (Map<?, ?>) type.getMethod("getMergedProperties").invoke(model);
                assertEquals(false, merged.get(KEY));
                assertFalse(merged.containsKey(LEGACY));
            }
            Object nestedCanonical = Config.getInstance().getMapper().convertValue(
                    Map.of(LEGACY, true, "additionalKafkaProperties", Map.of(KEY, false)), type);
            Map<?, ?> nestedMerged = (Map<?, ?>) type.getMethod("getMergedProperties").invoke(nestedCanonical);
            assertEquals(false, nestedMerged.get(KEY));
            Object legacy = Config.getInstance().getMapper().convertValue(Map.of(LEGACY, false), type);
            assertEquals(false, type.getMethod("getSchemaRegistryAutoRegisterSchemas").invoke(legacy));
        }
    }

    @Test
    void lightSerializerLooksUpWhenDisabledAndRegistersByDefault() throws Exception {
        for (Scenario scenario : scenarios()) {
            MockSchemaRegistryClient registry = new MockSchemaRegistryClient();
            AvroSerializer serializer = lightSerializer(scenario.settings(), registry);
            GenericData.Record record = record();
            if (!scenario.enabled()) {
                assertThrows(RuntimeException.class, () -> serializer.serialize(record));
                assertUnregistered(registry, record.getSchema().getFullName());
                registry.register(record.getSchema().getFullName(), new AvroSchema(record.getSchema()));
            }
            assertNotNull(serializer.serialize(record));
            assertEquals(1, registry.getAllSubjects().size());
            assertNull(serializer.serialize(null));
            // Initialization resolves the flag once; serialization does not parse it again.
            serializer.config.put(KEY, "not-a-boolean");
            assertNotNull(serializer.serialize(record));
        }
    }

    @Test
    void confluentSerializerReceivesEffectiveFlagFromEveryClient() throws Exception {
        for (Scenario scenario : scenarios()) {
            for (Map<String, Object> properties : clientProperties(scenario.settings())) {
                MockSchemaRegistryClient registry = new MockSchemaRegistryClient();
                try (KafkaAvroSerializer serializer = new KafkaAvroSerializer(registry)) {
                    serializer.configure(confluentSettings(properties), false);
                    GenericData.Record record = record();
                    if (!scenario.enabled()) {
                        assertThrows(SerializationException.class, () -> serializer.serialize("events", record));
                        assertUnregistered(registry, "events-value");
                        registry.register("events-value", new AvroSchema(record.getSchema()));
                    }
                    assertNotNull(serializer.serialize("events", record));
                    assertEquals(1, registry.getAllSubjects().size());
                }
            }
        }
    }

    @Test
    void invalidValuesRemainRawInSettersAndFailAtSerializerInitialization() {
        for (String key : List.of(KEY, LEGACY)) {
            Map<String, Object> raw = Map.of(key, "yes", "custom.setting", "preserved");
            for (Map<String, Object> properties : clientProperties(raw)) {
                assertEquals("yes", properties.get(KEY));
                assertEquals("preserved", properties.get("custom.setting"));
                assertFalse(properties.containsKey(LEGACY));
                try (KafkaAvroSerializer serializer = new KafkaAvroSerializer(new MockSchemaRegistryClient())) {
                    assertThrows(ConfigException.class, () -> serializer.configure(confluentSettings(properties), false));
                }
            }
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> lightSerializer(raw, new MockSchemaRegistryClient()));
            assertTrue(error.getMessage().contains(key));
            assertTrue(error.getMessage().contains("yes"));
            IllegalArgumentException direct = assertThrows(IllegalArgumentException.class,
                    () -> KafkaConfigUtils.autoRegisterSchemas(raw));
            assertTrue(direct.getMessage().contains("for " + key));
            assertTrue(direct.getMessage().contains("yes"));
        }
    }

    @Test
    void loadersAndReloadsResolveLegacyAndCanonicalSettings() {
        AtomicReference<String> yaml = new AtomicReference<>("properties:\n  " + LEGACY + ": false\n");
        withConfigSource(yaml, () -> {
            for (Map<String, Object> properties : loadedProperties()) {
                assertEquals(false, properties.get(KEY));
            }
            yaml.set("properties:\n  " + KEY + ": true\n  " + LEGACY + ": false\n");
            reloadAll();
            for (Map<String, Object> properties : loadedProperties()) {
                assertEquals(true, properties.get(KEY));
                assertFalse(properties.containsKey(LEGACY));
            }
        });
    }

    @Test
    void realReloadKeepsExistingSerializerSnapshotAndNewSerializerUsesUpdatedFlag() {
        for (boolean initiallyEnabled : new boolean[]{false, true}) {
            AtomicReference<String> yaml = new AtomicReference<>(
                    "properties:\n  " + LEGACY + ": " + initiallyEnabled + "\n");
            withConfigSource(yaml, () -> assertDoesNotThrow(() -> {
                KafkaStreamsConfig initialConfig = KafkaStreamsConfig.load();
                assertEquals(initiallyEnabled, initialConfig.getProperties().get(KEY));
                MockSchemaRegistryClient originalRegistry = new MockSchemaRegistryClient();
                AvroSerializer original = new AvroSerializer();
                original.schemaRegistry = originalRegistry;

                yaml.set("properties:\n  " + KEY + ": " + !initiallyEnabled + "\n");
                KafkaStreamsConfig.reload();
                KafkaStreamsConfig updatedConfig = KafkaStreamsConfig.load();
                assertNotSame(initialConfig, updatedConfig);
                assertEquals(!initiallyEnabled, updatedConfig.getProperties().get(KEY));

                MockSchemaRegistryClient updatedRegistry = new MockSchemaRegistryClient();
                AvroSerializer updated = new AvroSerializer();
                updated.schemaRegistry = updatedRegistry;

                // Observe actual registration/lookup after reload; neither serializer's map
                // is overwritten and neither expected result uses the production resolver.
                assertRegistrationBehavior(original, originalRegistry, initiallyEnabled);
                assertRegistrationBehavior(updated, updatedRegistry, !initiallyEnabled);
            }));
        }
    }

    private static void assertRegistrationBehavior(AvroSerializer serializer,
                                                    MockSchemaRegistryClient registry,
                                                    boolean registrationExpected) throws Exception {
        GenericData.Record record = record();
        if (!registrationExpected) {
            assertThrows(RuntimeException.class, () -> serializer.serialize(record));
            assertUnregistered(registry, record.getSchema().getFullName());
            registry.register(record.getSchema().getFullName(), new AvroSchema(record.getSchema()));
        }
        assertNotNull(serializer.serialize(record));
        assertEquals(1, registry.getAllSubjects().size());
    }

    @Test
    void invalidValuesDoNotPoisonRealLoadsReloadsOrLaterRecovery() {
        for (String key : List.of(KEY, LEGACY)) {
            AtomicReference<String> yaml = new AtomicReference<>(
                    "auditTopic: initial\nproperties:\n  " + key + ": yes\n");
            withConfigSource(yaml, () -> {
                assertRawInvalidLoad("yes", "initial");
                yaml.set("auditTopic: changed\nproperties:\n  " + key + ": invalid\n");
                reloadAll();
                assertRawInvalidLoad("invalid", "changed");
                yaml.set("auditTopic: recovered\nproperties:\n  " + KEY + ": false\n");
                reloadAll();
                for (Map<String, Object> properties : loadedProperties()) {
                    assertEquals(false, properties.get(KEY));
                }
                assertEquals("recovered", KafkaProducerConfig.load().getAuditTopic());
            });
        }
    }

    private static void assertRawInvalidLoad(String value, String topic) {
        // Repeat load to catch poisoned singleton/cache behavior.
        for (int attempt = 0; attempt < 2; attempt++) {
            for (Map<String, Object> properties : loadedProperties()) {
                assertEquals(value, properties.get(KEY));
                assertFalse(properties.containsKey(LEGACY));
            }
            assertEquals(topic, KafkaProducerConfig.load().getAuditTopic());
            assertEquals(topic, KafkaConsumerConfig.load().getAuditTopic());
            assertEquals(topic, KafkaStreamsConfig.load().getAuditTopic());
        }
    }

    private static void withConfigSource(AtomicReference<String> yaml, Runnable assertions) {
        Config config = Config.getInstance();
        config.setClassLoader(new ClassLoader(Config.class.getClassLoader()) {
            @Override
            public InputStream getResourceAsStream(String file) {
                for (String name : CONFIG_NAMES) {
                    if (file.equals(name + ".yml") || file.equals("config/" + name + ".yml")) {
                        return new ByteArrayInputStream(yaml.get().getBytes(StandardCharsets.UTF_8));
                    }
                }
                return super.getResourceAsStream(file);
            }
        });
        try {
            CONFIG_NAMES.forEach(config::clearConfigCache);
            assertions.run();
        } finally {
            config.setClassLoader(null);
            CONFIG_NAMES.forEach(config::clearConfigCache);
        }
    }

    private static void reloadAll() {
        KafkaProducerConfig.reload();
        KafkaStreamsConfig.reload();
        Config.getInstance().clearConfigCache("kafka-consumer");
    }

    private static List<Map<String, Object>> loadedProperties() {
        return List.of(KafkaProducerConfig.load().getProperties(), KafkaConsumerConfig.load().getProperties(),
                KafkaStreamsConfig.load().getProperties());
    }

    private static List<Map<String, Object>> clientProperties(Map<String, Object> input) {
        KafkaProducerConfig producer = new KafkaProducerConfig();
        KafkaConsumerConfig consumer = new KafkaConsumerConfig();
        KafkaStreamsConfig streams = new KafkaStreamsConfig();
        producer.setProperties(new HashMap<>(input));
        consumer.setProperties(new HashMap<>(input));
        streams.setProperties(new HashMap<>(input));
        return List.of(producer.getProperties(), consumer.getProperties(), streams.getProperties());
    }

    private static AvroSerializer lightSerializer(Map<String, Object> settings, MockSchemaRegistryClient registry) {
        Config config = Config.getInstance();
        Map<String, Object> source = new HashMap<>(settings);
        source.put("custom.setting", "present"); // Avoid falling through to producer defaults for an empty map.
        config.putInConfigCache("kafka-streams", Map.of("properties", source));
        try {
            AvroSerializer serializer = new AvroSerializer();
            serializer.schemaRegistry = registry;
            return serializer;
        } finally {
            config.clearConfigCache("kafka-streams");
        }
    }

    private static Map<String, Object> confluentSettings(Map<String, Object> properties) {
        Map<String, Object> settings = new HashMap<>(properties);
        settings.put("schema.registry.url", "mock://auto-register");
        return settings;
    }

    private static void assertUnregistered(MockSchemaRegistryClient registry, String subject) {
        RestClientException error = assertThrows(RestClientException.class, () -> registry.getAllVersions(subject));
        assertEquals(40401, error.getErrorCode());
    }

    private static GenericData.Record record() {
        Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"RegistrationTest\",\"fields\":[]}");
        return new GenericData.Record(schema);
    }
}
