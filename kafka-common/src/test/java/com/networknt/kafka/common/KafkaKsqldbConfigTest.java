package com.networknt.kafka.common;

import com.networknt.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KafkaKsqldbConfigTest {
    @Test
    public void testLoadingConfig() {
        KafkaKsqldbConfig config = (KafkaKsqldbConfig) Config.getInstance().getJsonObjectConfig(KafkaKsqldbConfig.CONFIG_NAME, KafkaKsqldbConfig.class);
        Assertions.assertNotNull(config);
    }
}
