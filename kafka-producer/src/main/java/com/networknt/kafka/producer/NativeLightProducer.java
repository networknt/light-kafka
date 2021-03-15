package com.networknt.kafka.producer;

import org.apache.kafka.clients.producer.Producer;

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
