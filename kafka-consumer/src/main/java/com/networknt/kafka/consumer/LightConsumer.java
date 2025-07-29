package com.networknt.kafka.consumer;

import com.networknt.config.Config;
import com.networknt.kafka.common.config.KafkaConsumerConfig;
import com.networknt.utility.ModuleRegistry;

import java.util.ArrayList;
import java.util.List;

public interface LightConsumer {
    void open();
    void close();

    /**
     * Register the module to the Registry so that the config can be shown in the server/info
     *
     */
    default void registerModule() {
        // register the module with the configuration properties.
        List<String> masks = new ArrayList<>();
        masks.add("basic.auth.user.info");
        masks.add("sasl.jaas.config");
        masks.add("schema.registry.ssl.truststore.password");
        ModuleRegistry.registerModule(KafkaConsumerConfig.CONFIG_NAME, LightConsumer.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(KafkaConsumerConfig.CONFIG_NAME), masks);
    }
}
