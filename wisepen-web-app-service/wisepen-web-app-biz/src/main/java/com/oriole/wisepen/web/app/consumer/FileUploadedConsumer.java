package com.oriole.wisepen.web.app.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.file.storage.api.domain.mq.FileUploadedMessage;
import com.oriole.wisepen.file.storage.api.enums.StorageSceneEnum;
import com.oriole.wisepen.file.storage.api.feign.RemoteStorageService;
import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageVersionEntity;
import com.oriole.wisepen.web.app.enums.SingleHtmlPageStatus;
import com.oriole.wisepen.web.app.repository.SingleHtmlPageVersionRepository;
import com.oriole.wisepen.web.app.service.ISingleHtmlPageService;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.plugins.kafka.asyncapi.annotations.KafkaAsyncOperationBinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.oriole.wisepen.file.storage.api.constant.MqTopicConstants.TOPIC_FILE_UPLOADED;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadedConsumer {

    private final SingleHtmlPageVersionRepository singleHtmlPageVersionRepository;
    private final ISingleHtmlPageService singleHtmlPageService;
    private final RemoteStorageService remoteStorageService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC_FILE_UPLOADED, groupId = "wisepen-web-app-upload-callback-group")
    @AsyncListener(operation = @AsyncOperation(
            channelName = TOPIC_FILE_UPLOADED,
            description = "消费 Single HTML Page 文件上传完成事件，校验内容并将版本推进到 READY",
            payloadType = FileUploadedMessage.class,
            message = @AsyncMessage(name = "FileUploadedMessage", title = "文件上传完成事件")
    ))
    @KafkaAsyncOperationBinding(groupId = "wisepen-web-app-upload-callback-group")
    public void onFileUploaded(String payload) throws Exception {
        FileUploadedMessage message = objectMapper.readValue(payload, FileUploadedMessage.class);
        log.info("single HTML page file upload event received. topic={} objectKey={} scene={}",
                TOPIC_FILE_UPLOADED, message.getObjectKey(), message.getScene());
        try {
            if (message.getScene() != StorageSceneEnum.PRIVATE_WEB_APP_ASSET
                    || Boolean.TRUE.equals(message.getFlashUploaded())) {
                log.debug("single HTML page file upload event skipped. objectKey={} scene={} flashUploaded={} reason=\"scene mismatch or flash upload\"",
                        message.getObjectKey(), message.getScene(), message.getFlashUploaded());
                return;
            }

            SingleHtmlPageVersionEntity version = singleHtmlPageVersionRepository
                    .findByObjectKey(message.getObjectKey()).orElse(null);
            if (version == null) {
                remoteStorageService.deleteFiles(List.of(message.getObjectKey()));
                log.warn("single HTML page file upload compensated for missing version. objectKey={}",
                        message.getObjectKey());
                return;
            }
            if (version.getSingleHtmlPageStatus() != SingleHtmlPageStatus.UPLOADING) {
                log.debug("single HTML page file upload event skipped. singleHtmlPageId={} resourceId={} version={} status={} reason=\"status mismatch\"",
                        version.getSingleHtmlPageId(), version.getResourceId(), version.getVersion(), version.getSingleHtmlPageStatus());
                return;
            }

            version.setSize(message.getSize());
            version.setSingleHtmlPageStatus(SingleHtmlPageStatus.UPLOADED);
            singleHtmlPageVersionRepository.save(version);
            singleHtmlPageService.finalizeToReady(version.getSingleHtmlPageId());
            log.info("single HTML page file upload finished. singleHtmlPageId={} resourceId={} version={} objectKey={} size={}",
                    version.getSingleHtmlPageId(), version.getResourceId(), version.getVersion(),
                    message.getObjectKey(), message.getSize());
        } catch (Exception e) {
            log.error("single HTML page file upload event consumption failed. topic={} objectKey={}",
                    TOPIC_FILE_UPLOADED, message.getObjectKey(), e);
            throw e;
        }
    }
}
