package com.oriole.wisepen.resource.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 集市订单号：由 {@code sellId} 与 {@code buyerId} 确定性生成，作为钱包流水 {@code relatedId}。
 */
public final class MarketOrderIds {

    private static final String PREFIX = "mkt:";

    private MarketOrderIds() {
    }

    public static long of(String sellId, String buyerId) {
        String composite = PREFIX + sellId + '\0' + buyerId;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(composite.getBytes(StandardCharsets.UTF_8));
            long value = ByteBuffer.wrap(digest, 0, Long.BYTES).getLong();
            return value & Long.MAX_VALUE;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
