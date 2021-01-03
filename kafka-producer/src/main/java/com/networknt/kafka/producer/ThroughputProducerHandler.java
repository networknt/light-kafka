package com.networknt.kafka.producer;

/**
 * This is a standard handler that can be used by any service endpoint or in the light-proxy to accept
 * Restful requests and push to a kafka topic.
 *
 * There are two running modes to choose from: throughput and guarantee.
 *
 * If you want to the highest throughput, you can use the TransactionalProducer and rely on the consumer
 * to let you know if any message is missed.
 *
 * If you want to guarantee the delivery, then you need to choose the guarantee so that the partition and
 * offset will be returned after the Kafka action.
 *
 * @author Steve Hu
 */
public class ThroughputProducerHandler {

}
