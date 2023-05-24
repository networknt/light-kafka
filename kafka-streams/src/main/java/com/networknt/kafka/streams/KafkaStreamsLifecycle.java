package com.networknt.kafka.streams;

import com.networknt.config.Config;
import com.networknt.kafka.common.KafkaStreamsConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaStreamsLifecycle implements LightStreams{

    private static final Logger logger= LoggerFactory.getLogger(KafkaStreamsLifecycle.class);

    static final KafkaStreamsConfig config = (KafkaStreamsConfig) Config.getInstance().getJsonObjectConfig(KafkaStreamsConfig.CONFIG_NAME, KafkaStreamsConfig.class);
    static final GenericTransformerStreamsConfig GENERIC_TRANSFORMER_STREAMS_CONFIG = (GenericTransformerStreamsConfig) Config.getInstance().getJsonObjectConfig(GenericTransformerStreamsConfig.CONFIG_NAME, GenericTransformerStreamsConfig.class);
    private KafkaStreams kafkaStreams;

    @Override
    public void start(String ip, int port) {

        Properties streamProps=new Properties();
        streamProps.putAll(config.getProperties());
        streamProps.put(StreamsConfig.APPLICATION_SERVER_CONFIG, ip +":"+port);
        streamProps.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());
        streamProps.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());

        try {
            kafkaStreams = new KafkaStreams(topology, streamProps);
            kafkaStreams.setUncaughtExceptionHandler(eh ->{
                logger.error("Kafka-Streams uncaught exception occurred. Stream will be replaced with new thread", eh);
                return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
            });
            if(config.isCleanUp()) {
                kafkaStreams.cleanUp();
            }
            kafkaStreams = startStream(ip, port, topology, config, genericStreamsTransformerTopology.getDlqTopicMetadataMap(), ConfigConstants.TRANSFORMER);

        }catch (Exception e){
            logger.error(e.getMessage());
            kafkaStreams = null;
        }
    }

    @Override
    public void close() {
        if(kafkaStreams !=null)
            kafkaStreams.close();

    }

    public KafkaStreams getKafkaStreams() {
        return kafkaStreams;
    }
}
