package com.oriole.wisepen.common.mq;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes Kafka messages with a Redis-backed retry backlog.
 */
public interface ReliablePublisher {

    /**
     * @param dedupKey Redis backlog deduplication key. Messages with the same key keep the latest payload.
     * @return future for observability only; callers do not need to await it.
     */
    CompletableFuture<Void> publish(String topic, String kafkaKey, Object payload, String dedupKey);
}
