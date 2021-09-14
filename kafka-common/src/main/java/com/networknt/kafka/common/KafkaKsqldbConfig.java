package com.networknt.kafka.common;

import java.util.List;
import java.util.Map;

public class KafkaKsqldbConfig {
    public static final String CONFIG_NAME = "kafka-ksqldb";
    private String ksqldbHost;
    private int ksqldbPort;
    private String query;
    private String backendUrl;
    private String backendPath;
    private String[] initialStreams;
    private String[] initialTables;
    private Map<String, Object> properties;

    private String trustStore;
    private String trustStorePassword;
    private String basicAuthCredentialsUser;
    private String basicAuthCredentialsPassword;
    private Boolean useTls;

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

    public String[] getInitialStreams() {
        return initialStreams;
    }

    public void setInitialStreams(String[]initialStreams) {
        this.initialStreams = initialStreams;
    }

    public String[] getInitialTables() {
        return initialTables;
    }

    public void setInitialTables(String[] initialTables) {
        this.initialTables = initialTables;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getBasicAuthCredentialsUser() {
        return basicAuthCredentialsUser;
    }

    public void setBasicAuthCredentialsUser(String basicAuthCredentialsUser) {
        this.basicAuthCredentialsUser = basicAuthCredentialsUser;
    }

    public String getBasicAuthCredentialsPassword() {
        return basicAuthCredentialsPassword;
    }

    public void setBasicAuthCredentialsPassword(String basicAuthCredentialsPassword) {
        this.basicAuthCredentialsPassword = basicAuthCredentialsPassword;
    }

    public Boolean isUseTls() {
        return useTls;
    }

    public void setUseTls(Boolean useTls) {
        this.useTls = useTls;
    }
}
