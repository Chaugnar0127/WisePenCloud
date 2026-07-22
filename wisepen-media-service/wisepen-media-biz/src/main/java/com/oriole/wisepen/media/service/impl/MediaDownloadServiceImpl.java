package com.oriole.wisepen.media.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.file.storage.api.feign.RemoteStorageService;
import com.oriole.wisepen.media.api.domain.dto.res.MediaDownloadJobResponse;
import com.oriole.wisepen.media.api.domain.mq.MediaWatermarkDownloadTaskMessage;
import com.oriole.wisepen.media.api.enums.*;
import com.oriole.wisepen.media.config.MediaProperties;
import com.oriole.wisepen.media.domain.entity.MediaDownloadJobEntity;
import com.oriole.wisepen.media.domain.entity.MediaInfoEntity;
import com.oriole.wisepen.media.domain.entity.MediaWatermarkSessionEntity;
import com.oriole.wisepen.media.exception.MediaError;
import com.oriole.wisepen.media.mq.KafkaMediaEventPublisher;
import com.oriole.wisepen.media.provider.MediaWatermarkProvider;
import com.oriole.wisepen.media.repository.MediaDownloadJobRepository;
import com.oriole.wisepen.media.repository.MediaInfoRepository;
import com.oriole.wisepen.media.repository.MediaWatermarkSessionRepository;
import com.oriole.wisepen.media.service.IMediaDownloadService;
import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaDownloadServiceImpl implements IMediaDownloadService {

    private static final DateTimeFormatter WATERMARK_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final CopyOptions IGNORE_NULL_COPY_OPTIONS = CopyOptions.create().ignoreNullValue();

    private final MediaInfoRepository mediaInfoRepository;
    private final MediaWatermarkSessionRepository watermarkSessionRepository;
    private final MediaDownloadJobRepository downloadJobRepository;
    private final KafkaMediaEventPublisher eventPublisher;
    private final RemoteStorageService remoteStorageService;
    private final MediaWatermarkProvider mediaWatermarkProvider;
    private final MediaProperties mediaProperties;

    @Override
    public MediaDownloadJobResponse createWatermarkDownloadJob(String resourceId, Long requesterId) {
        MediaInfoEntity mediaInfo = mediaInfoRepository.findByResourceId(resourceId)
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_NOT_FOUND));
        if (mediaInfo.getMediaStatus() == null || mediaInfo.getMediaStatus().getStatus() != MediaStatusEnum.READY) {
            throw new ServiceException(MediaError.MEDIA_PREVIEW_NOT_READY);
        }
        if (mediaInfo.getResourceType() == ResourceType.AUDIO) {
            throw new ServiceException(MediaError.MEDIA_WATERMARK_NOT_SUPPORTED);
        }

        LocalDateTime now = LocalDateTime.now();
        String sessionId = IdUtil.fastSimpleUUID();
        String jobId = IdUtil.fastSimpleUUID();

        // 带水印下载产物需要经过 provider/FFmpeg 重新生成，任务和取证会话须持久化，供前端轮询和失败追踪。
        MediaWatermarkSessionEntity session = MediaWatermarkSessionEntity.builder()
                .sessionId(sessionId)
                .wmId(IdUtil.fastSimpleUUID())
                .viewerId(requesterId)
                .resourceId(resourceId)
                .mediaId(mediaInfo.getMediaId())
                .purpose(WatermarkPurpose.DOWNLOAD)
                .accessedAt(now)
                .expiresAt(now.plusHours(mediaProperties.getDownloadJobTtlHours()))
                .watermarkText(requesterId + " " + now.format(WATERMARK_TIME_FORMAT) + " " + mediaProperties.getAcademicUseText())
                .deliveryMode(MediaDeliveryMode.DOWNLOAD_FILE)
                .status(WatermarkSessionStatus.PREPARING)
                .forensicStatus(ForensicStatus.PREPARING)
                .build();
        watermarkSessionRepository.save(session);

        MediaDownloadJobEntity job = MediaDownloadJobEntity.builder()
                .jobId(jobId)
                .sessionId(sessionId)
                .resourceId(resourceId)
                .mediaId(mediaInfo.getMediaId())
                .requesterId(requesterId)
                .status(MediaDownloadJobStatus.QUEUED)
                .expiresAt(now.plusHours(mediaProperties.getDownloadJobTtlHours()))
                .build();
        downloadJobRepository.save(job);

        eventPublisher.publishWatermarkDownloadTask(MediaWatermarkDownloadTaskMessage.builder()
                .jobId(jobId)
                .sessionId(sessionId)
                .resourceId(resourceId)
                .mediaId(mediaInfo.getMediaId())
                .build());

        log.info("media watermark download queued. jobId={} resourceId={} mediaId={}",
                jobId, resourceId, mediaInfo.getMediaId());
        return MediaDownloadJobResponse.builder()
                .jobId(job.getJobId())
                .sessionId(job.getSessionId())
                .resourceId(job.getResourceId())
                .mediaId(job.getMediaId())
                .status(job.getStatus())
                .downloadUrl(null)
                .failReason(job.getFailReason())
                .expiresAt(job.getExpiresAt())
                .build();
    }

    @Override
    public MediaDownloadJobResponse getDownloadJob(String jobId, Long requesterId) {
        MediaDownloadJobEntity job = downloadJobRepository.findById(jobId)
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_DOWNLOAD_JOB_NOT_FOUND));
        if (!requesterId.equals(job.getRequesterId())) {
            throw new ServiceException(MediaError.MEDIA_DOWNLOAD_JOB_NOT_FOUND);
        }
        String downloadUrl = null;
        if (job.getStatus() == MediaDownloadJobStatus.READY && StrUtil.isNotBlank(job.getOutputObjectKey())) {
            downloadUrl = remoteStorageService.getDownloadUrl(job.getOutputObjectKey(), null).getData();
        }
        return MediaDownloadJobResponse.builder()
                .jobId(job.getJobId())
                .sessionId(job.getSessionId())
                .resourceId(job.getResourceId())
                .mediaId(job.getMediaId())
                .status(job.getStatus())
                .downloadUrl(downloadUrl)
                .failReason(job.getFailReason())
                .expiresAt(job.getExpiresAt())
                .build();
    }

    @Override
    public String getOriginalDownloadUrl(String resourceId) {
        MediaInfoEntity mediaInfo = mediaInfoRepository.findByResourceId(resourceId)
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_NOT_FOUND));
        if (mediaInfo.getMediaStatus() == null || mediaInfo.getMediaStatus().getStatus() != MediaStatusEnum.READY) {
            throw new ServiceException(MediaError.MEDIA_PREVIEW_NOT_READY);
        }
        return remoteStorageService.getDownloadUrl(mediaInfo.getSourceObjectKey(), null).getData();
    }

    @Override
    public void handleWatermarkDownloadTask(MediaWatermarkDownloadTaskMessage message) {
        MediaDownloadJobEntity job = downloadJobRepository.findById(message.getJobId())
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_DOWNLOAD_JOB_NOT_FOUND));
        if (job.getStatus() != MediaDownloadJobStatus.QUEUED) {
            log.info("media watermark download skipped because status mismatched. jobId={} status={}",
                    job.getJobId(), job.getStatus());
            return;
        }

        MediaInfoEntity mediaInfo = mediaInfoRepository.findById(job.getMediaId())
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_NOT_FOUND));
        MediaWatermarkSessionEntity session = watermarkSessionRepository.findById(job.getSessionId())
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_PLAYBACK_SESSION_NOT_FOUND));

        BeanUtil.copyProperties(MediaDownloadJobEntity.builder()
                .status(MediaDownloadJobStatus.PROCESSING)
                .build(), job, IGNORE_NULL_COPY_OPTIONS);
        downloadJobRepository.save(job);
        try {
            // 暗水印嵌入和最终文件生成由 provider seam 负责，当前服务只推进任务状态和签发下载地址。
            String outputObjectKey = mediaWatermarkProvider.createWatermarkDownload(mediaInfo, session, job);
            BeanUtil.copyProperties(MediaDownloadJobEntity.builder()
                    .outputObjectKey(outputObjectKey)
                    .status(MediaDownloadJobStatus.READY)
                    .build(), job, IGNORE_NULL_COPY_OPTIONS);
            BeanUtil.copyProperties(MediaWatermarkSessionEntity.builder()
                    .status(WatermarkSessionStatus.FINISHED)
                    .forensicStatus(ForensicStatus.READY)
                    .build(), session, IGNORE_NULL_COPY_OPTIONS);
            watermarkSessionRepository.save(session);
            downloadJobRepository.save(job);
            log.info("media watermark download ready. jobId={} resourceId={} mediaId={}",
                    job.getJobId(), job.getResourceId(), job.getMediaId());
        } catch (Exception e) {
            BeanUtil.copyProperties(MediaDownloadJobEntity.builder()
                    .status(MediaDownloadJobStatus.FAILED)
                    .failReason(e.getMessage())
                    .build(), job, IGNORE_NULL_COPY_OPTIONS);
            BeanUtil.copyProperties(MediaWatermarkSessionEntity.builder()
                    .status(WatermarkSessionStatus.FAILED)
                    .forensicStatus(ForensicStatus.FAILED)
                    .build(), session, IGNORE_NULL_COPY_OPTIONS);
            watermarkSessionRepository.save(session);
            downloadJobRepository.save(job);
            log.warn("media watermark download failed. jobId={} resourceId={} mediaId={}",
                    job.getJobId(), job.getResourceId(), job.getMediaId(), e);
        }
    }

}
