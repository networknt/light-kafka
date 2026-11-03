package com.networknt.kafka.common;

import com.networknt.config.Config;
import com.networknt.config.ConfigInjection;
import com.networknt.config.schema.StringField;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.SslConfigs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Isolated("Temporarily changes the shared values injection map")
class KafkaSslEndpointIdentificationTest {
    static final String KEY = SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;
    static final List<String> CLIENTS = List.of("kafka-producer", "kafka-consumer", "kafka-streams");
    @TempDir Path directory;

    @Test
    void annotationsGeneratedResourcesAndPinnedConfigDefsAgree() throws Exception {
        for (Class<?> model : List.of(KafkaProducerPropertiesConfig.class,
                KafkaConsumerPropertiesConfig.class, KafkaStreamsPropertiesConfig.class)) {
            StringField annotation = model.getDeclaredField("sslEndpointIdentificationAlgorithm").getAnnotation(StringField.class);
            assertEquals(KEY, annotation.configFieldName());
            assertEquals("https", annotation.defaultValue());
            assertTrue(annotation.description().contains("explicit"));
        }
        for (ConfigDef def : List.of(ProducerConfig.configDef(), ConsumerConfig.configDef())) {
            assertNotNull(def.configKeys().get(KEY), "SSL key missing from pinned Kafka ConfigDef");
            assertEquals("https", def.configKeys().get(KEY).defaultValue);
        }
        ConfigDef confluent = new ConfigDef();
        SchemaRegistryClientConfig.withClientSslSupport(confluent, "schema.registry.");
        assertEquals("https", confluent.configKeys().get("schema.registry." + KEY).defaultValue);
        for (String client : CLIENTS) {
            copyResource(client + ".yaml");
            copyResource(client + "-schema.json");
            var schema = Config.getInstance().getJsonMapConfigNoCache(client + "-schema", directory.toString());
            Map<?, ?> properties = (Map<?, ?>) ((Map<?, ?>) schema.get("properties")).get("properties");
            Map<?, ?> definitions = (Map<?, ?>) properties.get("properties");
            assertEquals("https", ((Map<?, ?>) definitions.get(KEY)).get("default"));
            String yaml = Files.readString(directory.resolve(client + ".yml"));
            assertTrue(yaml.contains("${" + client + "." + KEY + ":https}"));
            assertEquals("https", loadedProperties(client).get(KEY));
        }
    }

    @Test
    void explicitEmptyAndHttpsValuesSurviveInjectionAndTypedLoading() throws Exception {
        Map<String, Object> original = ConfigInjection.getDecryptedValueMap();
        try {
            for (String value : List.of("", "https")) {
                Map<String, Object> overrides = new HashMap<>();
                for (String client : CLIENTS) {
                    copyResource(client + ".yaml");
                    overrides.put(client + "." + KEY, value);
                }
                ConfigInjection.setDecryptedValueMap(overrides);
                for (String client : CLIENTS) assertEquals(value, loadedProperties(client).get(KEY));
            }
        } finally {
            ConfigInjection.setDecryptedValueMap(original);
        }
    }

    @Test
    void actualTlsHandshakeAcceptsMatchingHostAndRejectsMismatchForEveryDefault() throws Exception {
        for (String client : CLIENTS) {
            copyResource(client + ".yaml");
            String algorithm = (String) loadedProperties(client).get(KEY);
            LocalTlsFixture.handshake(algorithm, "localhost");
            var failure = assertThrows(javax.net.ssl.SSLHandshakeException.class,
                    () -> LocalTlsFixture.handshake(algorithm, "wrong.invalid"));
            assertTrue(failure.getMessage().contains("wrong.invalid"), failure.toString());
        }
    }

    @Test
    void unknownAlgorithmsParseButFailHandshakeAndEmptyIsAnExplicitOptOut() throws Exception {
        ConfigDef def = new ConfigDef();
        SslConfigs.addClientSslSupport(def);
        for (String algorithm : List.of("also-name", "algo-name")) {
            assertEquals(algorithm, def.parse(Map.of(KEY, algorithm)).get(KEY));
            var failure = assertThrows(javax.net.ssl.SSLHandshakeException.class,
                    () -> LocalTlsFixture.handshake(algorithm, "localhost"));
            assertTrue(failure.getMessage().contains("Unknown identification algorithm"), failure.toString());
        }
        LocalTlsFixture.handshake("", "wrong.invalid");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadedProperties(String client) {
        var map = Config.getInstance().getJsonMapConfigNoCache(client, directory.toString());
        Map<String, Object> properties = (Map<String, Object>) map.get("properties");
        return switch (client) {
            case "kafka-producer" -> { var config = new KafkaProducerConfig(); config.setProperties(properties); yield config.getProperties(); }
            case "kafka-consumer" -> { var config = new KafkaConsumerConfig(); config.setProperties(properties); yield config.getProperties(); }
            case "kafka-streams" -> { var config = new KafkaStreamsConfig(); config.setProperties(properties); yield config.getProperties(); }
            default -> throw new IllegalArgumentException(client);
        };
    }

    private void copyResource(String name) throws Exception {
        try (var input = getClass().getResourceAsStream("/config/" + name)) {
            assertNotNull(input, name);
            Files.copy(input, directory.resolve(name.replace(".yaml", ".yml")), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
