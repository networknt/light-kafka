package com.networknt.kafka.common;

import com.networknt.config.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SchemaRegistryConfigTest {
    @Test
    public void testConfig() {
        SchemaRegistryConfig config = (SchemaRegistryConfig) Config.getInstance().getJsonObjectConfig(SchemaRegistryConfig.CONFIG_NAME, SchemaRegistryConfig.class);
        assertNotNull(config);
    }
}
