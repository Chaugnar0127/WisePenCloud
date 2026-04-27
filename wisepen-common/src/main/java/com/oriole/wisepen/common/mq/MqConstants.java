package com.oriole.wisepen.common.mq;

import java.time.Duration;

public final class MqConstants {

    public static final String BACKLOG_KEY_PREFIX = "wisepen:mq:backlog:";
    public static final String BACKLOG_LOCK_PREFIX = "wisepen:mq:backlog:lock:";
    public static final Duration BACKLOG_TTL = Duration.ofDays(7);
    public static final Duration DRAIN_LOCK_TTL = Duration.ofSeconds(30);

    private MqConstants() {
    }
}
