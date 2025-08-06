package com.networknt.kafka.streams;

import com.networknt.config.Config;
import com.networknt.kafka.common.config.KafkaStreamsConfig;
import com.networknt.kafka.entity.StreamsDLQMetadata;
import com.networknt.utility.ModuleRegistry;
import com.networknt.utility.ObjectUtils;
import com.networknt.utility.StringUtils;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
        ModuleRegistry.registerModule(KafkaStreamsConfig.CONFIG_NAME, LightStreams.class.getName(), Config.getNoneDecryptedInstance().getJsonMapConfigNoCache(KafkaStreamsConfig.CONFIG_NAME), masks);
    }

    /**
     * Get a value from a ReadOnlyKeyStore based on a given key.
     *
     * InvalidStateStoreException may be caused by a rebalance (transient),
     * so it is valid to retry the query.
     *
     * We try for a max of 30 seconds.
     *
     * @param keyValueStore - keystore we want to query.
     * @param key - key of the value we want to grab.
     * @return - return Object from keyValueStore (if any)
     */
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
        return returnObj;
    }

    /**
     * Get all from ReadOnlyKeyStore.
     *
     * InvalidStateStoreException may be caused by a rebalance (transient),
     * so it is valid to retry the query.
     *
     * We try for a max of 30 seconds.
     *
     * @param keyValueStore - keystore we want to query.
     * @return - return Object from keyValueStore (if any)
     */
    default Object getAllKafkaValue(ReadOnlyKeyValueStore<?, ?> keyValueStore) {
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
        return returnObj;
    }

    default KafkaStreams startStream(String ip, int port, Topology topology, KafkaStreamsConfig config, Map<String, StreamsDLQMetadata> dlqTopicSerdeMap, String... auditParentNames)throws RuntimeException {

        /**
         *This code base prepares topology by adding auditing and exception handling sinks to the topology.
         * Then starts the streams and returns the same
         */
        Properties streamProps = new Properties();
        streamProps.putAll(config.getKafkaMapProperties());
        streamProps.put(StreamsConfig.APPLICATION_SERVER_CONFIG, ip + ":" + port);


        if(config.isAuditEnabled() && !StringUtils.isEmpty(config.getAuditTarget()) && config.getAuditTarget().equalsIgnoreCase("topic")) {
            topology.addSink("AuditSink",
                    config.getAuditTopic(),
                    Serdes.String().serializer(),
                    Serdes.String().serializer(),
                    auditParentNames);
        }

        if(config.isDeadLetterEnabled()) {
            if(!ObjectUtils.isEmpty(dlqTopicSerdeMap) && !dlqTopicSerdeMap.isEmpty()) {
               dlqTopicSerdeMap.entrySet().forEach(
                        dlqTopicSerde -> {
                            topology.addSink(dlqTopicSerde.getKey().trim() + "_DLQSink",
                                    dlqTopicSerde.getKey().trim(),
                                    Serdes.ByteArray().serializer(),
                                    dlqTopicSerde.getValue().getSerde().serializer(),
                                    dlqTopicSerde.getValue().getParentNames().toArray(String[] :: new));
                        }
                );
            }
            else{
                throw new RuntimeException("DLQ is enabled, SreamsDLQMetadata can not be null");
            }

        }

        KafkaStreams kafkaStreams = new KafkaStreams(topology, streamProps);

        if (Boolean.TRUE.equals(config.getCleanUp())) {
            kafkaStreams.cleanUp();
        }
        kafkaStreams.start();

        return kafkaStreams;
    }
}
