package com.networknt.kafka.consumer;

import com.networknt.kafka.common.KafkaConsumerConfig;
import com.networknt.kafka.entity.ConsumerCommittedRequest;
import com.networknt.kafka.entity.ConsumerCommittedResponse;
import com.networknt.kafka.entity.ConsumerInstanceConfig;
import com.networknt.kafka.entity.ConsumerSeekRequest;
import com.networknt.kafka.entity.ConsumerSubscriptionRecord;
import com.networknt.kafka.entity.EmbeddedFormat;
import com.networknt.kafka.entity.TopicPartitionOffsetMetadata;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KafkaConsumerStateTest {

  @Test
  void seekWaitsForAGroupAssignmentBeforeChangingPosition() {
    TopicPartition partition = new TopicPartition("test-topic", 0);
    AssignmentOnTimedPollMockConsumer consumer = new AssignmentOnTimedPollMockConsumer(partition);
    KafkaConsumerState<byte[], byte[], byte[], byte[]> state = newState(consumer);
    state.subscribe(new ConsumerSubscriptionRecord(Collections.singletonList(partition.topic()), null));

    state.seek(new ConsumerSeekRequest(
            Collections.singletonList(new ConsumerSeekRequest.PartitionOffset(
                    partition.topic(), partition.partition(), 42L, "rollback")),
            Collections.emptyList()));

    assertTrue(consumer.receivedTimedPoll);
    assertEquals(42L, consumer.position(partition));
  }

  @Test
  void committedReturnsOffsetFromKafka42SetApi() {
    TopicPartition partition = new TopicPartition("test-topic", 0);
    SetCommittedOffsetMockConsumer consumer = new SetCommittedOffsetMockConsumer(
            partition, new OffsetAndMetadata(17L, "checkpoint"));
    KafkaConsumerState<byte[], byte[], byte[], byte[]> state = newState(consumer);

    ConsumerCommittedResponse response = state.committed(new ConsumerCommittedRequest(
            Collections.singletonList(new com.networknt.kafka.entity.TopicPartition(
                    partition.topic(), partition.partition()))));

    assertEquals(Collections.singletonList(new TopicPartitionOffsetMetadata(
            partition.topic(), partition.partition(), 17L, "checkpoint")), response.getOffsets());
    assertEquals(Collections.singleton(partition), consumer.requestedPartitions);
  }

  private static KafkaConsumerState<byte[], byte[], byte[], byte[]> newState(
          MockConsumer<byte[], byte[]> consumer) {
    KafkaConsumerConfig config = new KafkaConsumerConfig();
    config.setRequestTimeoutMs(1000);
    config.setUseNoWrappingAvro(false);
    return new KafkaConsumerState<>(
            config,
            ConsumerInstanceConfig.create(EmbeddedFormat.BINARY, EmbeddedFormat.BINARY),
            new ConsumerInstanceId("test-group", "test-instance"),
            consumer);
  }

  private static final class AssignmentOnTimedPollMockConsumer extends MockConsumer<byte[], byte[]> {
    private final TopicPartition partition;
    private boolean receivedTimedPoll;

    private AssignmentOnTimedPollMockConsumer(TopicPartition partition) {
      super("earliest");
      this.partition = partition;
    }

    @Override
    public synchronized ConsumerRecords<byte[], byte[]> poll(Duration timeout) {
      if (!timeout.isZero()) {
        receivedTimedPoll = true;
        if (assignment().isEmpty()) {
          rebalance(Collections.singleton(partition));
        }
      }
      return new ConsumerRecords<>(Map.of());
    }

    @Override
    public synchronized void seek(TopicPartition partition, OffsetAndMetadata offsetAndMetadata) {
      seek(partition, offsetAndMetadata.offset());
    }

    @Override
    public synchronized Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(
            Map<TopicPartition, Long> timestampsToSearch) {
      return Collections.emptyMap();
    }
  }

  private static final class SetCommittedOffsetMockConsumer extends MockConsumer<byte[], byte[]> {
    private final TopicPartition partition;
    private final OffsetAndMetadata offsetAndMetadata;
    private Set<TopicPartition> requestedPartitions;

    private SetCommittedOffsetMockConsumer(
            TopicPartition partition, OffsetAndMetadata offsetAndMetadata) {
      super("earliest");
      this.partition = partition;
      this.offsetAndMetadata = offsetAndMetadata;
    }

    @Override
    public synchronized Map<TopicPartition, OffsetAndMetadata> committed(
            Set<TopicPartition> partitions) {
      requestedPartitions = partitions;
      return Collections.singletonMap(partition, offsetAndMetadata);
    }
  }
}
