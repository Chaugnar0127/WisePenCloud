package com.oriole.wisepen.file.storage.service;

import com.oriole.wisepen.file.storage.api.domain.mq.FileUploadedMessage;

public interface IStorageEventPublisher {

    /**
     * 发布文件就绪事件 (供其他微服务绑定业务或触发转码)
     */
    void publishFileUploadedEvent(FileUploadedMessage fileUploadedMessage);
}