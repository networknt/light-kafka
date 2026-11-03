package com.networknt.kafka.common;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.types.Password;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Inventory against pinned upstream definitions; changes require an explicit audit update. */
class KafkaConfigurationDefaultsAuditTest {
    @Test
    void defaultsMatchTheReviewedInventory() throws Exception {
        ConfigDef confluent = AbstractKafkaSchemaSerDeConfig.baseConfigDef();
        Map<Class<?>, ConfigDef> models = Map.of(
                KafkaProducerPropertiesConfig.class, ProducerConfig.configDef(),
                KafkaConsumerPropertiesConfig.class, ConsumerConfig.configDef(),
                KafkaStreamsPropertiesConfig.class, StreamsConfig.configDef());
        List<String> rows = new ArrayList<>();
        for (var entry : models.entrySet()) {
            for (var field : entry.getKey().getDeclaredFields()) {
                for (Annotation annotation : field.getDeclaredAnnotations()) {
                    if (!annotation.annotationType().getPackageName().equals("com.networknt.config.schema")) continue;
                    String name = annotation.annotationType().getMethod("configFieldName").invoke(annotation).toString();
                    String value;
                    try {
                        value = annotation.annotationType().getMethod("defaultValue").invoke(annotation).toString();
                    } catch (NoSuchMethodException structuralField) { continue; }
                    ConfigDef.ConfigKey upstream = entry.getValue().configKeys().get(name);
                    String family = "Kafka";
                    if (upstream == null) { upstream = confluent.configKeys().get(name); family = "Confluent"; }
                    if (upstream == null && entry.getKey() == KafkaStreamsPropertiesConfig.class) {
                        upstream = ConsumerConfig.configDef().configKeys().get(name); family = "Kafka consumer (Streams forwarding)";
                    }
                    Object upstreamValue = upstream == null ? "<framework-only>" : upstream.defaultValue;
                    if (upstreamValue == ConfigDef.NO_DEFAULT_VALUE) upstreamValue = "<required>";
                    if (upstreamValue == null) upstreamValue = "<null>";
                    if (upstreamValue instanceof Password password) upstreamValue = password.value();
                    if (upstreamValue instanceof Class<?> type) upstreamValue = type.getName();
                    if (value.isEmpty()) value = "<empty>";
                    if (upstreamValue instanceof String text && text.isEmpty()) upstreamValue = "<empty>";
                    rows.add(entry.getKey().getSimpleName() + "\t" + name + "\t" + value
                            + "\t" + (upstream == null ? "Framework" : family) + "\t" + upstreamValue);
                }
            }
        }
        rows.sort(Comparator.naturalOrder());
        rows.add(0, "model\tproperty\tframework default\tupstream definition\tupstream default");
        Path root = Path.of(System.getProperty("basedir")).getParent();
        Files.write(root.resolve("kafka-common/target/kafka-config-defaults.tsv"), rows);
        assertEquals(Files.readAllLines(root.resolve("doc/kafka-config-defaults.tsv")), rows,
                "Review pinned upstream/default changes and update the documented inventory");
    }
}
