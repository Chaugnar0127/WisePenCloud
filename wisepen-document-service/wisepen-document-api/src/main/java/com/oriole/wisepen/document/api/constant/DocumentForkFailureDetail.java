package com.oriole.wisepen.document.api.constant;

/**
 * 文档 Fork 失败时写入 {@code ResourceForkCompletedMessage#errorMessage} 的明细文案。
 */
public interface DocumentForkFailureDetail {

    String DOCUMENT_EXISTS_BUT_NOT_READY = "document exists but not READY";
    String SOURCE_DOCUMENT_NOT_READY = "source document not READY";
}
