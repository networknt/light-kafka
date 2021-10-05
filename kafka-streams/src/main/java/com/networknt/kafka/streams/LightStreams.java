package com.networknt.kafka.streams;

import com.networknt.config.Config;
import com.networknt.kafka.common.KafkaStreamsConfig;
import com.networknt.utility.ModuleRegistry;

import java.util.ArrayList;
import java.util.List;

public interface LightStreams {
    /**
     * Start the streams processing. The ip and port is for remote queries if the data is not
     * on the current instance.
     *
     * @param ip ip address of the instance
     * @param port port number that the service is bound
     */
    void start(String ip, int port);
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
        ModuleRegistry.registerModule(LightStreams.class.getName(), Config.getInstance().getJsonMapConfigNoCache(KafkaStreamsConfig.CONFIG_NAME), masks);
    }
}
