package com.networknt.kafka.common;

import java.util.Map;

/**
 * A Kafka setting configuration file. It get from defined resource yml file in
 * resources/config folder or externalized config folder. This config is for both
 * producer and consumer and for each individual application, it might have only
 * producer or consumer depending on the nature of the application.
 *
 * @author Steve Hu
 */
public class KafkaProducerConfig {
    public static final String CONFIG_NAME = "kafka-producer";
    private Map<String, Object> properties;

    private String topic;
    private boolean injectOpenTracing;
    private boolean injectCallerId;

    public KafkaProducerConfig() {
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isInjectOpenTracing() {
        return injectOpenTracing;
    }

    public void setInjectOpenTracing(boolean injectOpenTracing) {
        this.injectOpenTracing = injectOpenTracing;
    }

    public boolean isInjectCallerId() {
        return injectCallerId;
    }

    public void setInjectCallerId(boolean injectCallerId) {
        this.injectCallerId = injectCallerId;
    }
}
