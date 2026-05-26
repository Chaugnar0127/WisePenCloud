package com.oriole.wisepen.note.api.constant;

/**
 * 笔记 Fork 失败时写入 {@code ResourceForkCompletedMessage#errorMessage} 的明细文案。
 */
public interface NoteForkFailureDetail {

    String SOURCE_NOTE_NOT_FOUND = "source note not found";
    String TARGET_VERSION_NOT_FOUND = "target version not found";
}
