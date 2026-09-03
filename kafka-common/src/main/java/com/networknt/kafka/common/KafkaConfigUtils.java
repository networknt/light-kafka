package com.networknt.kafka.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class KafkaConfigUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfigUtils.class);
    private static final String ADDITIONAL_KAFKA_PROPERTIES_KEY = "additionalKafkaProperties";
    private static final String GROUP_ID_KEY = "group.id";
    private static final String SASL_JAAS_CONFIG_KEY = "sasl.jaas.config";
    private static final String SASL_JAAS_CONFIG_MODULE_KEY = "sasl.jaas.config.module";
    private static final String SASL_JAAS_CONFIG_USERNAME_KEY = "sasl.jaas.config.username";
    private static final String SASL_JAAS_CONFIG_PASSWORD_KEY = "sasl.jaas.config.password";

    protected KafkaConfigUtils() {
        // Utility class
    }

    /**
     * Takes a field from a map and converts it to the specified type using the provided ObjectMapper.
     * <b>NOTE:</b> We should probably move this method to the core l4j project eventually. This utility method will be used by all config classes.
     *
     * @param mappedConfig - the map that contains the configuration
     * @param mapper       - the ObjectMapper to use for conversion
     * @param key          - the key in the map to look for the value
     * @param type         - the class type to convert the value to
     * @param <T>          - the type to convert the value to
     * @return - the value converted to the specified type, or null if the value is not found or conversion fails
     */
    public static <T> T getFromMappedConfigAsType(final Map<String, Object> mappedConfig, final ObjectMapper mapper, final String key, final Class<T> type) {
        final var value = mappedConfig.get(key);
        if (value == null) {
            LOG.error("Could not find property '{}' in mappedConfig", key);
            return null;
        }
        try {
            return mapper.convertValue(value, type);
        } catch (Exception e) {
            LOG.error("Could not convert property '{}' into type '{}'", key, type.getCanonicalName(), e);
            return null;
        }

    }

    public static void addIfSet(final Map<String, Object> map, final String key, final Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    static Map<String, Object> getJsonMapConfig(final String configName, final Map<String, Object> fallback) {
        try {
            return Config.getInstance().getJsonMapConfig(configName);
        } catch (ClassCastException e) {
            // A 2.3.0 client may already have cached the same file as a configuration POJO.
            // Reuse our parsed snapshot when possible; only bypass the shared cache on first load.
            return fallback != null ? fallback : Config.getInstance().getJsonMapConfigNoCache(configName);
        }
    }

    static boolean sameMappedConfig(final Map<String, Object> left, final Map<String, Object> right) {
        return left == right || left != null && left.equals(right);
    }

    static void copyProperty(final Map<String, Object> source, final Map<String, Object> target,
                             final String sourceKey, final String targetKey) {
        if (source.containsKey(sourceKey)) {
            target.put(targetKey, source.get(sourceKey));
        }
    }

    /**
     * Normalizes the raw Kafka properties used by both Config object mapping and the
     * explicit Kafka configuration loaders. Kafka client properties are intentionally
     * open-ended, so unknown keys must be preserved instead of being rejected by a
     * typed configuration class.
     *
     * @param rawProperties raw value of the {@code properties} configuration field
     * @return normalized Kafka client properties
     */
    static Map<String, Object> normalizeKafkaProperties(final Object rawProperties) {
        Map<String, Object> properties = new HashMap<>();
        if (!(rawProperties instanceof Map<?, ?> rawMap)) {
            return properties;
        }

        Object additionalProperties = rawMap.get(ADDITIONAL_KAFKA_PROPERTIES_KEY);
        if (additionalProperties instanceof Map<?, ?> additionalMap) {
            copyStringEntries(additionalMap, properties);
        }

        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof String key
                    && !ADDITIONAL_KAFKA_PROPERTIES_KEY.equals(key)
                    && entry.getValue() != null) {
                properties.put(key, entry.getValue());
            }
        }

        if (properties.containsKey(GROUP_ID_KEY)) {
            properties.put(GROUP_ID_KEY, String.valueOf(properties.get(GROUP_ID_KEY)));
        }

        Object module = properties.get(SASL_JAAS_CONFIG_MODULE_KEY);
        Object username = properties.get(SASL_JAAS_CONFIG_USERNAME_KEY);
        Object password = properties.get(SASL_JAAS_CONFIG_PASSWORD_KEY);
        if (!rawMap.containsKey(SASL_JAAS_CONFIG_KEY)
                && module != null
                && username != null
                && password != null) {
            properties.put(SASL_JAAS_CONFIG_KEY,
                    createSaslJaasConfigProperty(
                            String.valueOf(module), String.valueOf(username), String.valueOf(password)));
        }

        properties.remove(SASL_JAAS_CONFIG_MODULE_KEY);
        properties.remove(SASL_JAAS_CONFIG_USERNAME_KEY);
        properties.remove(SASL_JAAS_CONFIG_PASSWORD_KEY);
        return properties;
    }

    /**
     * Normalizes a public setter value while retaining the caller's mutable map
     * instance, as the 2.3.0 configuration API did.
     */
    static Map<String, Object> normalizeKafkaPropertiesInPlace(final Map<String, Object> rawProperties) {
        Map<String, Object> normalized = normalizeKafkaProperties(rawProperties);
        if (rawProperties.equals(normalized)) {
            return rawProperties;
        }
        try {
            rawProperties.clear();
            rawProperties.putAll(normalized);
            return rawProperties;
        } catch (UnsupportedOperationException e) {
            return normalized;
        }
    }

    private static void copyStringEntries(final Map<?, ?> source, final Map<String, Object> target) {
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() instanceof String key) {
                addIfSet(target, key, entry.getValue());
            }
        }
    }

    public static String createSaslJaasConfigProperty(final String module, final String username, final String password) {
        if (module == null || username == null || password == null) {
            var printedPass = password != null ? password.substring(0, Math.min(5, password.length())) : null;
            LOG.error("module, username, and password must not be null when creating the sasl.jaas.config property. (module = {}, username = {}, password={})", module, username, printedPass);
            return null;
        }
        return module + " required username=" + '"' + username + '"' + " password=" + '"' + password + '"' + ';';
    }
}
