/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.networknt.kafka.consumer;

import com.networknt.exception.FrameworkException;
import com.networknt.kafka.common.KafkaConsumerConfig;
import com.networknt.kafka.entity.ConsumerRecord;
import com.networknt.status.Status;
import jdk.jshell.Snippet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * State for tracking the progress of a single consumer read request.
 *
 * <p>To support embedded formats that require translation between the format deserialized
 * by the Kafka decoder and the format returned in the ConsumerRecord entity sent back to
 * the client, this class uses two pairs of key-value generic type parameters: KafkaK/KafkaV
 * is the format returned by the Kafka consumer's decoder/deserializer, ClientK/ClientV is
 * the format returned to the client in the HTTP response. In some cases these may be identical.
 */
public class KafkaConsumerReadTask<KafkaKeyT, KafkaValueT, ClientKeyT, ClientValueT> {

  private static final Logger log = LoggerFactory.getLogger(KafkaConsumerReadTask.class);
  public static final String UNEXPECTED_CONSUMER_READ_EXCEPTION = "ERR12205";

  private final KafkaConsumerState<KafkaKeyT, KafkaValueT, ClientKeyT, ClientValueT> parent;
  private final Duration requestTimeout;
  // the minimum bytes the task should accumulate
  // before returning a response (or hitting the timeout)
  // responseMinBytes might be bigger than maxResponseBytes
  // in cases where the functionality is disabled
  private final int responseMinBytes;
  private final long maxResponseBytes;
  private final ConsumerReadCallback<ClientKeyT, ClientValueT> callback;
  private boolean finished;

  private List<ConsumerRecord<ClientKeyT, ClientValueT>> messages;
  private long bytesConsumed = 0;
  private boolean exceededMinResponseBytes = false;
  private boolean exceededMaxResponseBytes = false;
  private final Instant started;
  private final Clock clock = Clock.systemUTC();


  public KafkaConsumerReadTask(
      KafkaConsumerState<KafkaKeyT, KafkaValueT, ClientKeyT, ClientValueT> parent,
      Duration timeout,
      long maxBytes,
      ConsumerReadCallback<ClientKeyT, ClientValueT> callback,
      KafkaConsumerConfig config
  ) {
    this.parent = parent;
    this.maxResponseBytes =
        Math.min(maxBytes, config.getRequestMaxBytes());
    Duration defaultRequestTimeout =
        parent.getConsumerInstanceConfig().getRequestWaitMs() != null
            ? Duration.ofMillis(parent.getConsumerInstanceConfig().getRequestWaitMs())
            : Duration.ofMillis(config.getRequestTimeoutMs());
    this.requestTimeout =
        timeout.isNegative() || timeout.isZero()
            ? defaultRequestTimeout
            : Collections.min(Arrays.asList(timeout, defaultRequestTimeout));
    int responseMinBytes =
        parent.getConsumerInstanceConfig().getResponseMinBytes() != null
            ? parent.getConsumerInstanceConfig().getResponseMinBytes()
            : config.getFetchMinBytes();
    this.responseMinBytes = responseMinBytes < 0 ? Integer.MAX_VALUE : responseMinBytes;


    this.callback = callback;
    this.finished = false;

    started = clock.instant();
  }

  /**
   * Performs one iteration of reading from a consumer iterator.
   */
  public void doPartialRead() {
    try {
      // Initial setup requires locking, which must be done on this thread.
      if (messages == null) {
        messages = new Vector<>();
      }
      addRecords();

      log.trace(
          "KafkaConsumerReadTask exiting read with id={} messages={} bytes={}, backing off if not"
              + " complete",
          this,
          messages.size(),
          bytesConsumed
      );

      Instant now = clock.instant();
      Duration elapsed = Duration.between(started, now);

      // Including the rough message size here ensures processing finishes if the next
      // message exceeds the maxResponseBytes
      boolean requestTimedOut = elapsed.compareTo(requestTimeout) >= 0;
      if (requestTimedOut || exceededMaxResponseBytes || exceededMinResponseBytes) {
        log.trace(
            "Finishing KafkaConsumerReadTask id={} requestTimedOut={} "
                + "exceededMaxResponseBytes={} exceededMinResponseBytes={}",
            this,
            requestTimedOut,
            exceededMaxResponseBytes,
            exceededMinResponseBytes
        );
        finish();
      }
    } catch (Exception e) {
      Status status = new Status(UNEXPECTED_CONSUMER_READ_EXCEPTION, this);
      finish(new FrameworkException(status));
      e.printStackTrace();
      log.error("Unexpected exception in consumer read task", e);
    }
  }

  public boolean isDone() {
    return finished;
  }

  /**
   * Polls for and reads records until either the minimum response bytes are filled,
   *  the maximum response bytes will be reached, or no more records can be read from polling.
   */
  private void addRecords() {
    while (!exceededMinResponseBytes && !exceededMaxResponseBytes && parent.hasNext()) {
      synchronized (parent) {
        if (parent.hasNext()) {
          maybeAddRecord();
        }
      }
    }
    while (!exceededMaxResponseBytes && parent.hasNextCached()) {
      synchronized (parent) {
        if (parent.hasNextCached()) {
          maybeAddRecord();
        }
      }
    }
  }

  /**
   * Tries to add the latest record from the iterator
   * to the read records if it doesn't go over the maximum response bytes.
   * Keeps track and marks when we are about to exceed the max response bytes, and
   * have exceeded the min response bytes
   */
  private void maybeAddRecord() {
    ConsumerRecordAndSize<ClientKeyT, ClientValueT> recordAndSize =
            parent.createConsumerRecord(parent.peek());
    long roughMsgSize = recordAndSize.getSize();
    if (bytesConsumed + roughMsgSize >= maxResponseBytes) {
      this.exceededMaxResponseBytes = true;
      return;
    }

    messages.add(recordAndSize.getRecord());
    parent.next(); // increment iterator
    bytesConsumed += roughMsgSize;
    if (!exceededMinResponseBytes && bytesConsumed > responseMinBytes) {
      this.exceededMinResponseBytes = true;
    }
  }

  void finish() {
    finish(null);
  }

  private void finish(FrameworkException e) {
    log.trace("Finishing KafkaConsumerReadTask id={}", this, e);
    try {
      callback.onCompletion((e == null) ? messages : null, e);
    } catch (Throwable t) {
      // This protects the worker thread from any issues with the callback code. Nothing to be
      // done here but log it since it indicates a bug in the calling code.
      log.error("Consumer read callback threw an unhandled exception ", t);
    }
    finished = true;
  }
}
