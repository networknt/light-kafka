package com.networknt.kafka.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.config.Config;
import com.networknt.config.ConfigInjection;
import com.networknt.config.schema.NumberField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConsumerFetchConfigTest {
    private static final String KEY = "max.partition.fetch.bytes";

    @Test
    void annotationAndGeneratedResourcesAgree() throws Exception {
        NumberField field = KafkaConsumerPropertiesConfig.class.getDeclaredField("maxPartitionFetchBytes")
                .getAnnotation(NumberField.class);
        assertEquals("102400", field.defaultValue());
        assertEquals(KEY, field.configFieldName());
        try (var yaml = getClass().getResourceAsStream("/config/kafka-consumer.yaml");
             var schema = getClass().getResourceAsStream("/config/kafka-consumer-schema.json")) {
            assertNotNull(yaml);
            assertNotNull(schema);
            String text = new String(yaml.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(text.contains("  " + KEY + ": ${kafka-consumer." + KEY + ":" + field.defaultValue() + "}"));
            var property = new ObjectMapper().readTree(schema).path("properties").path("properties")
                    .path("properties").path(KEY);
            assertEquals(102400, property.path("default").decimalValue().intValueExact());
            assertEquals(field.description(), property.path("description").asText());
        }
    }

    @Test
    void loadsDefaultAndExternalOverrideInIsolatedJvm(@TempDir Path directory) throws Exception {
        Path output = directory.resolve("probe.log");
        Process child = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                KafkaConsumerFetchConfigTest.class.getName())
                .redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(child.waitFor(30, TimeUnit.SECONDS), "Configuration probe timed out");
            assertEquals(0, child.exitValue(), () -> {
                try { return Files.readString(output); }
                catch (java.io.IOException e) { return e.toString(); }
            });
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly();
                child.waitFor();
            }
        }
    }

    public static void main(String[] args) {
        // This process owns its injection map and cache; the test runner's globals are untouched.
        ConfigInjection.setDecryptedValueMap(new HashMap<>());
        Config.getInstance().clearConfigCache("kafka-consumer");
        assertEquals(102400, new KafkaConsumerConfig("kafka-consumer").getKafkaMapProperties().get(KEY));
        ConfigInjection.setDecryptedValueMap(new HashMap<>(Map.of("kafka-consumer." + KEY, 65536)));
        Config.getInstance().clearConfigCache("kafka-consumer");
        assertEquals(65536, new KafkaConsumerConfig("kafka-consumer").getKafkaMapProperties().get(KEY));
    }
}
