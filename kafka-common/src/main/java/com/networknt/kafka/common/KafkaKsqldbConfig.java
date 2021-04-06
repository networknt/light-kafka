package com.networknt.kafka.common;

import java.util.Map;

public class KafkaKsqldbConfig {
    public static final String CONFIG_NAME = "kafka-ksqldb";
    private String ksqldbHost;
    private int ksqldbPort;
    private String query;
    private String backendUrl;
    private String backendPath;
    private Map<String, Object> properties;

    public KafkaKsqldbConfig() {
    }

    public String getKsqldbHost() {
        return ksqldbHost;
    }

    public void setKsqldbHost(String ksqldbHost) {
        this.ksqldbHost = ksqldbHost;
    }

    public int getKsqldbPort() {
        return ksqldbPort;
    }

    public void setKsqldbPort(int ksqldbPort) {
        this.ksqldbPort = ksqldbPort;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public void setBackendUrl(String backendUrl) {
        this.backendUrl = backendUrl;
    }

    public String getBackendPath() {
        return backendPath;
    }

    public void setBackendPath(String backendPath) {
        this.backendPath = backendPath;
    }
}
