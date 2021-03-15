package com.networknt.kafka.producer;

import com.google.protobuf.ByteString;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ProducerService {
    /**
     * Produce the given record to Kafka.
     */
    CompletableFuture<ProduceResult> produce(
            String topicName,
            Optional<Integer> partitionId,
            Map<String, Optional<ByteString>> headers,
            Optional<ByteString> key,
            Optional<ByteString> value,
            Instant timestamp);
}
