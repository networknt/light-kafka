package com.networknt.kafka.producer;

import com.networknt.kafka.common.KafkaProducerConfig;
import io.undertow.server.HttpServerExchange;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.header.Headers;

public interface NativeLightProducer extends LightProducer {
    /**
     * Return the Kafka Producer instance so that it can be used to produce message to a Kafka topic.
     * Instead of using a queue like QueuedLightProducer to hide the producer from the user, this
     * interface will provide the native producer to work with.
     *
     * @return Kafka producer instance
     */
    Producer getProducer();
}
