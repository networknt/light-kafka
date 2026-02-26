package com.networknt.kafka.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class KafkaConfigUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfigUtils.class);

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

    public static String createSaslJaasConfigProperty(final String module, final String username, final String password) {
        if (module == null || username == null || password == null) {
            var printedPass = password != null ? password.substring(0, 5) : null;
            LOG.error("module, username, and password must not be null when creating the sasl.jaas.config property. (module = {}, key = {}, password={})", module, username, printedPass);
            return null;
        }
        return module + " required username=" + '"' + username + '"' + " password=" + '"' + password + '"' + ';';
    }
}
