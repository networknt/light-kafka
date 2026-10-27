package com.networknt.kafka.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.networknt.config.Config;
import com.networknt.config.ConfigInjection;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AutoRegisterSchemasTest {
    private static final String KEY = "auto.register.schemas";
    private static final String LEGACY = "schema.registry.auto.register.schemas";

    private record Scenario(Map<String, Object> input, Object canonical, boolean confluentRegisters, boolean lightRegisters) { }

    private static List<Scenario> scenarios() {
        return List.of(
                new Scenario(Map.of(KEY, false), false, false, false),
                new Scenario(Map.of(LEGACY, false), null, true, false),
                new Scenario(Map.of(KEY, "false"), "false", false, false),
                new Scenario(Map.of(LEGACY, "false"), null, true, false),
                new Scenario(Map.of(KEY, false, LEGACY, true), false, false, false),
                new Scenario(Map.of(KEY, true, LEGACY, false), true, true, true),
                new Scenario(Map.of(KEY, " TRUE ", LEGACY, "false"), " TRUE ", true, true),
                new Scenario(Map.of(KEY, false, LEGACY, "yes"), false, false, false),
                new Scenario(Map.of(LEGACY, "TrUe"), null, true, true),
                new Scenario(Map.of("additionalKafkaProperties", Map.of(LEGACY, false)), null, true, false),
                new Scenario(Map.of(LEGACY, true, "additionalKafkaProperties", Map.of(KEY, false)), false, false, false),
                new Scenario(Map.of(), null, true, true));
    }

    @Test
    void allClientsPreserveLegacyWithoutTranslatingItAndKeepCanonicalSettings() {
        for (Scenario scenario : scenarios()) {
            Map<String, Object> source = new HashMap<>(scenario.input());
            source.put("custom.setting", "preserved");
            for (Map<String, Object> properties : clientProperties(source)) {
                assertEquals(scenario.canonical(), properties.get(KEY));
                assertEquals(scenario.canonical() != null, properties.containsKey(KEY));
                Object legacy = scenario.input().get(LEGACY);
                if (legacy == null && scenario.input().get("additionalKafkaProperties") instanceof Map<?, ?> additional) {
                    legacy = additional.get(LEGACY);
                }
                assertEquals(legacy, properties.get(LEGACY));
                assertFalse(properties.containsKey("additionalKafkaProperties"));
                assertEquals("preserved", properties.get("custom.setting"));
            }
        }
    }

    @Test
    void legacyWarningExplainsConfluentDoesNotSupportTheKey() {
        Logger logger = (Logger) LoggerFactory.getLogger(KafkaConfigUtils.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            clientProperties(Map.of(KEY, false));
            assertTrue(appender.list.isEmpty(), "Canonical configuration should not warn");
            clientProperties(Map.of(LEGACY, false));
            clientProperties(Map.of(KEY, true, LEGACY, false));
            assertEquals(6, appender.list.size(), "One warning per client normalization");
            for (int i = 0; i < appender.list.size(); i++) {
                ILoggingEvent event = appender.list.get(i);
                assertEquals(Level.WARN, event.getLevel());
                assertEquals(LEGACY + " has no effect on Confluent serializers; use " + KEY
                        + ". Only Light's Avro serializer supports the old key",
                        event.getFormattedMessage());
            }
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void confluentSerializerRegistersOrLooksUpFromEveryClient() throws Exception {
        for (Scenario scenario : scenarios()) {
            for (Map<String, Object> properties : clientProperties(scenario.input())) {
                MockSchemaRegistryClient registry = new MockSchemaRegistryClient();
                try (KafkaAvroSerializer serializer = new KafkaAvroSerializer(registry)) {
                    serializer.configure(confluentSettings(properties), false);
                    GenericData.Record record = record();
                    if (!scenario.confluentRegisters()) {
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
    void lightSerializerAloneRetainsLegacyFallbackWithCanonicalPrecedence() throws Exception {
        for (Scenario scenario : scenarios()) {
            MockSchemaRegistryClient registry = new MockSchemaRegistryClient();
            AvroSerializer serializer = lightSerializer(scenario.input(), registry);
            GenericData.Record record = record();
            if (!scenario.lightRegisters()) {
                RuntimeException error = assertThrows(RuntimeException.class, () -> serializer.serialize(record));
                assertInstanceOf(RestClientException.class, error.getCause());
                assertUnregistered(registry, record.getSchema().getFullName());
                registry.register(record.getSchema().getFullName(), new AvroSchema(record.getSchema()));
            }
            assertNotNull(serializer.serialize(record));
            assertNull(serializer.serialize(null));
        }
    }

    @Test
    void invalidRawValuesArePreservedByConfigsAndRejectedBySerializers() {
        for (String key : List.of(KEY, LEGACY)) {
            for (Object value : List.of("yes", 1)) {
                Map<String, Object> input = Map.of(key, value);
                for (Map<String, Object> properties : clientProperties(input)) {
                    assertEquals(value, properties.get(key));
                    try (KafkaAvroSerializer serializer = new KafkaAvroSerializer(new MockSchemaRegistryClient())) {
                        if (KEY.equals(key)) {
                            assertThrows(ConfigException.class, () -> serializer.configure(confluentSettings(properties), false));
                        } else {
                            // The unsupported legacy value must not change Confluent's default.
                            serializer.configure(confluentSettings(properties), false);
                            assertNotNull(serializer.serialize("events", record()));
                        }
                    }
                }
                AvroSerializer serializer = lightSerializer(input, new MockSchemaRegistryClient());
                RuntimeException error = assertThrows(RuntimeException.class, () -> serializer.serialize(record()));
                IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, error.getCause());
                assertEquals("Invalid value '" + value + "' for " + key + "; expected true or false", cause.getMessage());
            }
        }
    }

    @Test
    void canonicalValuesOverridesWorkAndOldOverridesLeaveTheDefaultUnchanged() {
        Map<String, Object> original = ConfigInjection.getDecryptedValueMap();
        Map<String, Object> overrides = original == null ? new HashMap<>() : new HashMap<>(original);
        List<String> clients = List.of("kafka-producer", "kafka-consumer", "kafka-streams");
        try {
            for (String client : clients) {
                overrides.remove(client + "." + KEY);
                overrides.put(client + "." + LEGACY, false);
            }
            ConfigInjection.setDecryptedValueMap(overrides);
            for (String client : clients) {
                Map<?, ?> properties = (Map<?, ?>) Config.getInstance().getJsonMapConfigNoCache(client).get("properties");
                assertEquals(true, properties.get(KEY), "Old override should not populate the new placeholder");
                assertFalse(properties.containsKey(LEGACY));
            }
            for (String client : clients) overrides.put(client + "." + KEY, false);
            ConfigInjection.setDecryptedValueMap(overrides);
            for (String client : clients) {
                Map<?, ?> properties = (Map<?, ?>) Config.getInstance().getJsonMapConfigNoCache(client).get("properties");
                assertEquals(false, properties.get(KEY));
            }
        } finally {
            ConfigInjection.setDecryptedValueMap(original);
        }
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
        AvroSerializer serializer = new AvroSerializer();
        serializer.config = KafkaConfigUtils.normalizeKafkaProperties(settings);
        serializer.schemaRegistry = registry;
        return serializer;
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
        Schema schema = new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"RegistrationTest\",\"fields\":[]}");
        return new GenericData.Record(schema);
    }
}
