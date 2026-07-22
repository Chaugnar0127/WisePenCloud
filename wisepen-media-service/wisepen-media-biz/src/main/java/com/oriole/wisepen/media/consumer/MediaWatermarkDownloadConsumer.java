package com.oriole.wisepen.media.consumer;

import com.oriole.wisepen.media.api.domain.mq.MediaWatermarkDownloadTaskMessage;
import com.oriole.wisepen.media.service.IMediaDownloadService;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.plugins.kafka.asyncapi.annotations.KafkaAsyncOperationBinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.oriole.wisepen.media.api.constant.MqTopicConstants.TOPIC_MEDIA_WATERMARK_DOWNLOAD;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaWatermarkDownloadConsumer {

    private final IMediaDownloadService mediaDownloadService;

    @KafkaListener(topics = TOPIC_MEDIA_WATERMARK_DOWNLOAD, groupId = "wisepen-media-watermark-download-group")
    @AsyncListener(operation = @AsyncOperation(
            channelName = TOPIC_MEDIA_WATERMARK_DOWNLOAD,
            description = "消费媒体带水印下载任务，生成会话级取证水印下载产物。",
            payloadType = MediaWatermarkDownloadTaskMessage.class,
            message = @AsyncMessage(name = "MediaWatermarkDownloadTaskMessage", title = "媒体带水印下载任务")
    ))
    @KafkaAsyncOperationBinding(groupId = "wisepen-media-watermark-download-group")
    public void onWatermarkDownload(MediaWatermarkDownloadTaskMessage message) {
        log.info("media watermark download event received. topic={} jobId={} mediaId={}",
                TOPIC_MEDIA_WATERMARK_DOWNLOAD, message.getJobId(), message.getMediaId());
        try {
            mediaDownloadService.handleWatermarkDownloadTask(message);
            log.debug("media watermark download event consumed. topic={} jobId={} mediaId={}",
                    TOPIC_MEDIA_WATERMARK_DOWNLOAD, message.getJobId(), message.getMediaId());
        } catch (Exception e) {
            log.error("media watermark download event consumption failed. topic={} jobId={} mediaId={}",
                    TOPIC_MEDIA_WATERMARK_DOWNLOAD, message.getJobId(), message.getMediaId(), e);
            throw e;
        }
    }
}
