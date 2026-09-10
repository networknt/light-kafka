package com.networknt.kafka.common;

import com.networknt.config.Config;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigReloadTest {
    @Test
    void producerRetainsChangedSourceAfterReload() throws Exception {
        assertReload(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class,
                KafkaProducerConfig::load, KafkaProducerConfig::reload, KafkaProducerConfig::getAuditTopic, false);
    }

    @Test
    void streamsRetainsChangedSourceAfterReload() throws Exception {
        assertReload(KafkaStreamsConfig.CONFIG_NAME, KafkaStreamsConfig.class,
                KafkaStreamsConfig::load, KafkaStreamsConfig::reload, KafkaStreamsConfig::getAuditTopic, false);
    }

    @Test
    void waitingProducerLoadCannotUndoReload() throws Exception {
        assertReload(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class,
                KafkaProducerConfig::load, KafkaProducerConfig::reload, KafkaProducerConfig::getAuditTopic, true);
    }

    @Test
    void waitingStreamsLoadCannotUndoReload() throws Exception {
        assertReload(KafkaStreamsConfig.CONFIG_NAME, KafkaStreamsConfig.class,
                KafkaStreamsConfig::load, KafkaStreamsConfig::reload, KafkaStreamsConfig::getAuditTopic, true);
    }

    private static <T> void assertReload(String name, Class<T> lock, Supplier<T> load,
                                         Runnable reload, Function<T, String> topic, boolean concurrent) throws Exception {
        AtomicReference<String> content = new AtomicReference<>("auditTopic: before-reload\nproperties: {}\n");
        Config config = Config.getInstance();
        config.setClassLoader(new ClassLoader(Config.class.getClassLoader()) {
            @Override
            public InputStream getResourceAsStream(String file) {
                if (file.equals(name + ".yml") || file.equals("config/" + name + ".yml")) {
                    return new ByteArrayInputStream(content.get().getBytes(StandardCharsets.UTF_8));
                }
                return super.getResourceAsStream(file);
            }
        });
        Thread reader = null;
        try {
            T initial = load.get();
            assertEquals("before-reload", topic.apply(initial));
            if (concurrent) {
                // Force a reader to capture an intermediate map and wait for the reload monitor.
                content.set("auditTopic: intermediate\nproperties: {}\n");
                config.clearConfigCache(name);
                FutureTask<T> pending = new FutureTask<>(load::get);
                reader = new Thread(pending, "config-reload-reader");
                synchronized (lock) {
                    reader.start();
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                    while (reader.getState() != Thread.State.BLOCKED && System.nanoTime() < deadline) {
                        Thread.yield();
                    }
                    assertEquals(Thread.State.BLOCKED, reader.getState());
                    content.set("auditTopic: after-reload\nproperties: {}\n");
                    reload.run();
                }
                assertEquals("after-reload", topic.apply(pending.get(5, TimeUnit.SECONDS)));
            } else {
                content.set("auditTopic: after-reload\nproperties: {}\n");
                reload.run();
            }
            T refreshed = load.get();
            assertNotSame(initial, refreshed);
            assertEquals("after-reload", topic.apply(refreshed));
            assertSame(refreshed, load.get());
            assertEquals("after-reload", config.getJsonMapConfig(name).get("auditTopic"));
        } finally {
            if (reader != null) {
                reader.join(5000);
                assertFalse(reader.isAlive(), "Config reader did not terminate");
            }
            config.setClassLoader(null);
            config.clearConfigCache(name);
        }
    }
}
