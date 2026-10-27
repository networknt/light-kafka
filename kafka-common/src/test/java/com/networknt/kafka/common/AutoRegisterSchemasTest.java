package com.networknt.kafka.common;

import com.networknt.config.Config;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AutoRegisterSchemasTest {
    private static final String KEY = "auto.register.schemas";
    private static final String LEGACY = "schema.registry.auto.register.schemas";

    @Test
    void allClientsNormalizeBothSpellingsAndPreserveOtherProperties() {
        for (Map<String, Object> input : new Map[]{
                Map.of(KEY, false), Map.of(LEGACY, false),
                Map.of(KEY, "false"), Map.of(LEGACY, "false"),
                Map.of(KEY, false, LEGACY, true),
                Map.of("additionalKafkaProperties", Map.of(LEGACY, false)),
                Map.of(LEGACY, true, "additionalKafkaProperties", Map.of(KEY, false))}) {
            Map<String, Object> source = new HashMap<>(input);
            source.put("custom.setting", "preserved");
            for (Map<String, Object> result : clientProperties(source)) {
                assertEquals(false, result.get(KEY));
                assertFalse(result.containsKey(LEGACY));
                assertEquals("preserved", result.get("custom.setting"));
            }
        }
        assertTrue(KafkaConfigUtils.autoRegisterSchemas(Map.of()));
        assertTrue(KafkaConfigUtils.autoRegisterSchemas(Map.of(KEY, "TRUE")));
        assertThrows(IllegalArgumentException.class,
                () -> KafkaConfigUtils.normalizeKafkaProperties(Map.of(KEY, "invalid")));
    }

    @Test
    void typedModelsKeepLegacyAccessorsAndCanonicalPrecedenceInEitherOrder() throws Exception {
        for (Class<?> type : new Class[]{KafkaProducerPropertiesConfig.class,
                KafkaConsumerPropertiesConfig.class, KafkaStreamsPropertiesConfig.class}) {
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
        for (Map<String, Object> settings : new Map[]{Map.of(KEY, false), Map.of(LEGACY, false),
                Map.of(KEY, "false"), Map.of(KEY, false, LEGACY, true), Map.of(), Map.of(KEY, true)}) {
            MockSchemaRegistryClient registry = new MockSchemaRegistryClient();
            AvroSerializer serializer = new AvroSerializer();
            serializer.config = settings;
            serializer.schemaRegistry = registry;
            GenericData.Record record = record();
            if (!KafkaConfigUtils.autoRegisterSchemas(settings)) {
                assertThrows(RuntimeException.class, () -> serializer.serialize(record));
                assertUnregistered(registry, record.getSchema().getFullName());
                registry.register(record.getSchema().getFullName(), new AvroSchema(record.getSchema()));
            }
            assertNotNull(serializer.serialize(record));
            assertEquals(1, registry.getAllSubjects().size());
            assertNull(serializer.serialize(null));
        }
    }

    @Test
    void confluentSerializerReceivesEffectiveFlagFromEveryClient() throws Exception {
        for (boolean enabled : new boolean[]{false, true}) {
            for (Map<String, Object> properties : clientProperties(Map.of(LEGACY, String.valueOf(enabled)))) {
                MockSchemaRegistryClient registry = new MockSchemaRegistryClient();
                Map<String, Object> settings = new HashMap<>(properties);
                settings.put("schema.registry.url", "mock://auto-register");
                try (KafkaAvroSerializer serializer = new KafkaAvroSerializer(registry)) {
                    serializer.configure(settings, false);
                    GenericData.Record record = record();
                    if (!enabled) {
                        assertThrows(org.apache.kafka.common.errors.SerializationException.class,
                                () -> serializer.serialize("events", record));
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
    void loadersAndReloadsResolveLegacyAndCanonicalSettings() {
        Config config = Config.getInstance();
        java.util.concurrent.atomic.AtomicReference<String> yaml =
                new java.util.concurrent.atomic.AtomicReference<>(
                        "properties:\n  schema.registry.auto.register.schemas: false\n");
        java.util.Set<String> names = java.util.Set.of("kafka-producer", "kafka-consumer", "kafka-streams");
        config.setClassLoader(new ClassLoader(Config.class.getClassLoader()) {
            @Override
            public java.io.InputStream getResourceAsStream(String file) {
                for (String name : names) {
                    if (file.equals(name + ".yml") || file.equals("config/" + name + ".yml")) {
                        return new java.io.ByteArrayInputStream(
                                yaml.get().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    }
                }
                return super.getResourceAsStream(file);
            }
        });
        try {
            names.forEach(config::clearConfigCache);
            assertEquals(false, KafkaProducerConfig.load().getProperties().get(KEY));
            assertEquals(false, KafkaConsumerConfig.load().getProperties().get(KEY));
            assertEquals(false, KafkaStreamsConfig.load().getProperties().get(KEY));
            yaml.set("properties:\n  auto.register.schemas: true\n  schema.registry.auto.register.schemas: false\n");
            KafkaProducerConfig.reload();
            KafkaStreamsConfig.reload();
            config.clearConfigCache("kafka-consumer");
            for (Map<String, Object> properties : java.util.List.of(
                    KafkaProducerConfig.load().getProperties(), KafkaConsumerConfig.load().getProperties(),
                    KafkaStreamsConfig.load().getProperties())) {
                assertEquals(true, properties.get(KEY));
                assertFalse(properties.containsKey(LEGACY));
            }
        } finally {
            config.setClassLoader(null);
            names.forEach(config::clearConfigCache);
        }
    }

    private static java.util.List<Map<String, Object>> clientProperties(Map<String, Object> input) {
        KafkaProducerConfig producer = new KafkaProducerConfig();
        KafkaConsumerConfig consumer = new KafkaConsumerConfig();
        KafkaStreamsConfig streams = new KafkaStreamsConfig();
        producer.setProperties(new HashMap<>(input));
        consumer.setProperties(new HashMap<>(input));
        streams.setProperties(new HashMap<>(input));
        return java.util.List.of(producer.getProperties(), consumer.getProperties(), streams.getProperties());
    }

    private static void assertUnregistered(MockSchemaRegistryClient registry, String subject) {
        var error = assertThrows(io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException.class,
                () -> registry.getAllVersions(subject));
        assertEquals(40401, error.getErrorCode());
    }

    private static GenericData.Record record() {
        Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"RegistrationTest\",\"fields\":[]}");
        return new GenericData.Record(schema);
    }
}
