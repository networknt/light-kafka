package com.networknt.kafka.consumer;

import com.networknt.config.Config;
import com.networknt.kafka.common.KafkaConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractConsumer implements LightConsumer {
    static private final Logger logger = LoggerFactory.getLogger(AbstractConsumer.class);

    /** indicate the thread should be stopped */
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    public KafkaConsumer<byte[], byte[]> consumer;

    static private Properties consumerProps;
    static final KafkaConsumerConfig config = (KafkaConsumerConfig) Config.getInstance().getJsonObjectConfig(KafkaConsumerConfig.CONFIG_NAME, KafkaConsumerConfig.class);
    static {
        consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getKeyDeserializer());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getValueDeserializer());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getAutoOffsetReset());
        consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, config.getIsolationLevel());
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.isEnableAutoCommit());
        consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, config.getAutoCommitIntervalMs());
    }

    @Override
    public void open() {
        consumer = new KafkaConsumer<>(consumerProps);
    }

    @Override
    public void close() {
        stopped.getAndSet(true);
        consumer.close();
    }
}
