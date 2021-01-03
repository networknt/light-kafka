package com.networknt.kafka.producer;

import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.kafka.common.KafkaProducerConfig;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

public class GuaranteeProducerHandler implements LightHttpHandler {
    static private final Logger logger = LoggerFactory.getLogger(GuaranteeProducerHandler.class);

    static private Properties producerProps;
    static String callerId = "unknown";
    static final KafkaProducerConfig config = (KafkaProducerConfig) Config.getInstance().getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);
    static {
        producerProps = new Properties();
        // create producer properties
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getKeySerializer());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getValueSerializer());
        // create safe producer
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,  config.isEnableIdempotence());
        producerProps.put(ProducerConfig.ACKS_CONFIG, config.getAcks());
        producerProps.put(ProducerConfig.RETRIES_CONFIG, config.getRetries());
        producerProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, config.getMaxInFlightRequestsPerConnection());

        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, config.getBatchSize());
        producerProps.put(ProducerConfig.LINGER_MS_CONFIG, config.getLingerMs());
        producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, config.getBufferMemory());
        if(config.isInjectCallerId()) {
            Map<String, Object> serverConfig = Config.getInstance().getJsonMapConfigNoCache("server");
            if(serverConfig != null) {
                callerId = (String)serverConfig.get("serviceId");
            }
        }
    }
    static final String topic = config.getTopic();

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {


        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.setStatusCode(201);
        exchange.getResponseSender().send("{}");
    }

    public KafkaProducer<byte[], byte[]> createKafkaProducer() {
        return new KafkaProducer<>(producerProps);
    }

}
