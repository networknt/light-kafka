package com.networknt.kafka.producer;

import com.networknt.config.Config;
import com.networknt.httpstring.AttachmentConstants;
import com.networknt.httpstring.HttpStringConstants;
import com.networknt.kafka.common.KafkaProducerConfig;
import com.networknt.utility.Constants;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.undertow.server.HttpServerExchange;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This is the guaranteed producer to ensure that the message is acknowledged from the Kafka brokers
 * before the service is respond to the consumer call. Although this producer is not the best one
 * for high throughput batch producing, it is the safest one. Once the caller receives the successful
 * response from the service, it can make sure that the message is on the Kafka cluster.
 *
 * @author Steve Hu
 */
public class SidecarProducer implements NativeLightProducer {
    static private final Logger logger = LoggerFactory.getLogger(SidecarProducer.class);
    static private Properties producerProps;
    public static final KafkaProducerConfig config = (KafkaProducerConfig) Config.getInstance().getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);
    static {
        producerProps = new Properties();
        producerProps.put("bootstrap.servers", config.getBootstrapServers());
        producerProps.put("acks", config.getAcks());
        producerProps.put("retries", config.getRetries());
        producerProps.put("batch.size", config.getBatchSize());
        producerProps.put("linger.ms", config.getLingerMs());
        producerProps.put("buffer.memory", config.getBufferMemory());
        producerProps.put("key.serializer", config.getKeySerializer());
        producerProps.put("value.serializer", config.getValueSerializer());
    }

    public Producer<byte[], byte[]> producer;

    @Override
    public void open() {
        producer = new KafkaProducer<>(producerProps, new ByteArraySerializer(), new ByteArraySerializer());
    }

    @Override
    public Producer getProducer() {
        return producer;
    }


    @Override
    public void close() {
        if(producer != null) {
            producer.close();
        }
    }
}
