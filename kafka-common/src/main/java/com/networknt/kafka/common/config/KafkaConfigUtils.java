package com.networknt.kafka.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class KafkaConfigUtils {

    /**
     * Takes a field from a map and converts it to the specified type using the provided ObjectMapper.
     * <b>NOTE:</b> We should probably move this method to the core l4j project eventually. This utility method will be used by all config classes.
     *
     * @param mappedConfig - the map that contains the configuration
     * @param mapper - the ObjectMapper to use for conversion
     * @param key - the key in the map to look for the value
     * @param type - the class type to convert the value to
     * @return - the value converted to the specified type, or null if the value is not found or conversion fails
     * @param <T> - the type to convert the value to
     */
    public static <T> T getFromMappedConfigAsType(final Map<String, Object> mappedConfig, final ObjectMapper mapper, final String key, final Class<T> type) {
        final var value = mappedConfig.get(key);
        if (value == null)
            return null;
        try {
            return mapper.convertValue(value, type);
        } catch (Exception e) {
            return null;
        }

    }

    public static void addIfSet(final Map<String, Object> map, final String key, final Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
