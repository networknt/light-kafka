package com.networknt.kafka.consumer;

import com.networknt.config.Config;
import com.networknt.kafka.common.config.KafkaConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractConsumer implements LightConsumer {
    static private final Logger logger = LoggerFactory.getLogger(AbstractConsumer.class);

    /** indicate the thread should be stopped */
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    public KafkaConsumer<byte[], byte[]> consumer;

    static private Map<String, Object> properties;

    static final KafkaConsumerConfig config = KafkaConsumerConfig.load();

    @Override
    public void open() {
        consumer = new KafkaConsumer<>(config.getKafkaMapProperties());
    }

    @Override
    public void close() {
        stopped.getAndSet(true);
        if(consumer != null) consumer.close();
    }
}
