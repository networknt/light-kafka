package com.networknt.kafka.streams;

import org.apache.kafka.streams.KafkaStreams;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class KafkaStreamsRegistry {
    private static final Map<String, KafkaStreams> registry = new ConcurrentHashMap<>();

    public static void register(String name, KafkaStreams streams) {
        registry.put(name, streams);
    }

    public static Map<String, KafkaStreams> getRegistry() {
        return registry;
    }
}
