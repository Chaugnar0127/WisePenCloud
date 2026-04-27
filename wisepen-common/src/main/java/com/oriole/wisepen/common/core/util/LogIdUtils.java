package com.oriole.wisepen.common.core.util;

import java.util.List;

/**
 * 日志中领域 ID 摘要工具。
 * <p>
 * 列表小于等于 {@link #MAX_INLINE_IDS} 时直接打印；否则截断为前 N 项并附带 total，
 * 避免在循环/批量操作中把整个大列表写进日志。
 */
public final class LogIdUtils {

    private static final int MAX_INLINE_IDS = 10;

    private LogIdUtils() {
    }

    public static String summarizeIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return "[]";
        }
        if (ids.size() <= MAX_INLINE_IDS) {
            return ids.toString();
        }
        return ids.subList(0, MAX_INLINE_IDS) + "... total=" + ids.size();
    }
}
