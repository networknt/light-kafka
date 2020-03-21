package com.networknt.kafka.common;

import java.util.List;

public class SchemaRegistryConfig {
    public static final String CONFIG_NAME = "schema-registry";

    List<String> serverUrls;
    boolean autoRegisterSchema;

    public SchemaRegistryConfig() {
    }

    public List<String> getServerUrls() {
        return serverUrls;
    }

    public void setServerUrls(List<String> serverUrls) {
        this.serverUrls = serverUrls;
    }

    public boolean isAutoRegisterSchema() {
        return autoRegisterSchema;
    }

    public void setAutoRegisterSchema(boolean autoRegisterSchema) {
        this.autoRegisterSchema = autoRegisterSchema;
    }
}
