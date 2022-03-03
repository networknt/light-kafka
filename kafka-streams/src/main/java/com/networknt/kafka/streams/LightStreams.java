package com.networknt.kafka.streams;

import com.networknt.config.Config;
import com.networknt.kafka.common.KafkaStreamsConfig;
import com.networknt.utility.ModuleRegistry;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public interface LightStreams {
    Logger logger = LoggerFactory.getLogger(LightStreams.class);
    long WAIT_THRESHOLD = 30000;
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

    default Object getKafkaValueByKey(ReadOnlyKeyValueStore<String, ?> keyValueStore, String key) {
        Object returnObj = null;
        boolean storeMoved;
        long timeout = System.currentTimeMillis() + WAIT_THRESHOLD;
        do {
            if(System.currentTimeMillis() >= timeout) {
                if(logger.isDebugEnabled()) logger.debug("Timeout period is passed after 30 seconds.");
                break;
            }
            try {
                returnObj = keyValueStore.get(key);
                storeMoved = false;
            } catch (InvalidStateStoreException e) {
                storeMoved = true;
                try {
                    logger.debug(e.getMessage());
                    Thread.sleep(100L);
                } catch (InterruptedException ie) {
                    logger.error(ie.getMessage(), ie);
                }
            }
        } while (storeMoved);
        if(returnObj == null) {
            logger.debug("Timeout occurred trying to get key '{}' from stream.", key);
        }
        return returnObj;
    }

    default Object getAllKafkaValue(ReadOnlyKeyValueStore<String, ?> keyValueStore) {
        Object returnObj = null;
        boolean storeMoved;
        long timeout = System.currentTimeMillis() + WAIT_THRESHOLD;
        do {
            if(System.currentTimeMillis() >= timeout) {
                if(logger.isDebugEnabled()) logger.debug("Timeout period is passed after 30 seconds.");
                break;
            }
            try {
                returnObj = keyValueStore.all();
                storeMoved = false;
            } catch (InvalidStateStoreException e) {
                storeMoved = true;
                try {
                    logger.debug(e.getMessage());
                    Thread.sleep(100L);
                } catch (InterruptedException ie) {
                    logger.error(ie.getMessage(), ie);
                }
            }
        } while (storeMoved);
        if(returnObj == null) {
            logger.debug("Timeout occurred trying to get 'all' from stream.");
        }
        return returnObj;
    }
}
