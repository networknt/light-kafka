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
    static String callerId = "unknown";
    static final KafkaProducerConfig config = (KafkaProducerConfig) Config.getInstance().getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);
    static {
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
        return new KafkaProducer<>(config.getProperties());
    }

}
