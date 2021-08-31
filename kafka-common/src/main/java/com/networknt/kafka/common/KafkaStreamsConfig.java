package com.networknt.kafka.common;

import java.util.Map;

public class KafkaStreamsConfig {
    public static final String CONFIG_NAME = "kafka-streams";

    String applicationId;
    boolean cleanUp;
    private Map<String, Object> properties;

    public KafkaStreamsConfig() {
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public boolean isCleanUp() {
        return cleanUp;
    }

    public void setCleanUp(boolean cleanUp) {
        this.cleanUp = cleanUp;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
