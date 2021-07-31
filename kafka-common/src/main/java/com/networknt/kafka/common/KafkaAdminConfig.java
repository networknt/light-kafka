package com.networknt.kafka.common;

import java.util.Map;

/**
 * A configuration class for the Kafka AdminClient.
 *
 * @author Steve Hu
 */
public class KafkaAdminConfig {
    public static final String CONFIG_NAME = "kafka-admin";
    private Map<String, Object> properties;

    public KafkaAdminConfig() {
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
