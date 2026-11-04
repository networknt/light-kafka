package com.networknt.kafka.common;

import tools.jackson.databind.ObjectMapper;
import com.networknt.config.Config;
import com.networknt.config.ConfigInjection;
import com.networknt.config.schema.NumberField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConsumerFetchConfigTest {
    private static final String KEY = "max.partition.fetch.bytes";

    @Test
    void annotationAndGeneratedSchemaAgree() throws Exception {
        NumberField field = KafkaConsumerPropertiesConfig.class.getDeclaredField("maxPartitionFetchBytes")
                .getAnnotation(NumberField.class);
        assertEquals("102400", field.defaultValue());
        assertEquals(KEY, field.configFieldName());
        try (var schema = getClass().getResourceAsStream("/config/kafka-consumer-schema.json")) {
            assertNotNull(schema);
            var property = new ObjectMapper().readTree(schema).path("properties").path("properties")
                    .path("properties").path(KEY);
            assertTrue(property.isObject(), "Schema property not found at /properties/properties/properties/" + KEY);
            assertTrue(property.path("default").isNumber(), "Schema default must be numeric for " + KEY);
            assertEquals(102400, property.path("default").decimalValue().intValueExact());
            assertEquals(field.description(), property.path("description").asString());
        }
    }

    /**
     * Runs the configuration probe in a child JVM with a 120-second default timeout.
     * For slower environments, pass {@code -Dkafka.fetch.config.probe.timeout.seconds=240}
     * to Maven. The value must be a positive integer number of seconds; malformed or
     * non-positive values fail the test instead of silently selecting the default.
     */
    @Test
    void loadsDefaultAndExternalOverrideInIsolatedJvm(@TempDir Path directory) throws Exception {
        Path output = directory.resolve("probe.log");
        String timeoutProperty = "kafka.fetch.config.probe.timeout.seconds";
        String timeoutValue = System.getProperty(timeoutProperty, "120");
        long timeoutSeconds;
        try {
            timeoutSeconds = Long.parseLong(timeoutValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(timeoutProperty + " must be a positive integer: " + timeoutValue, e);
        }
        assertTrue(timeoutSeconds > 0, timeoutProperty + " must be a positive integer: " + timeoutValue);
        // Intentionally use a fresh JVM without parent -D configuration overrides or agents:
        // this probes the packaged YAML fallback, not the runner's deployment configuration.
        Process child = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                KafkaConsumerFetchConfigTest.class.getName())
                .redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(child.waitFor(timeoutSeconds, TimeUnit.SECONDS), "Configuration probe timed out");
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
