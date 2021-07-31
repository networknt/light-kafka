package com.networknt.kafka.common;

import com.networknt.config.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KafkaAdminConfigTest {
    @Test
    public void testLoadingConfig() {
        KafkaAdminConfig config = (KafkaAdminConfig) Config.getInstance().getJsonObjectConfig(KafkaAdminConfig.CONFIG_NAME, KafkaAdminConfig.class);
        assertNotNull(config);
    }
}
