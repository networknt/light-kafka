package com.networknt.kafka.producer;

import com.networknt.config.Config;
import com.networknt.kafka.common.KafkaProducerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static final KafkaProducerConfig config = (KafkaProducerConfig) Config.getInstance().getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);

    public Producer<byte[], byte[]> producer;

    @Override
    public void open() {
        producer = new KafkaProducer<>(config.getProperties());
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
