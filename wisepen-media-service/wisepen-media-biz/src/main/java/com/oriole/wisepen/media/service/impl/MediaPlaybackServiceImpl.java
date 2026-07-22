package com.oriole.wisepen.media.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.file.storage.api.feign.RemoteStorageService;
import com.oriole.wisepen.media.api.domain.dto.res.MediaPlaybackSessionResponse;
import com.oriole.wisepen.media.api.enums.*;
import com.oriole.wisepen.media.cache.RedisCacheManager;
import com.oriole.wisepen.media.config.MediaProperties;
import com.oriole.wisepen.media.domain.MediaPlaybackGrant;
import com.oriole.wisepen.media.domain.entity.MediaInfoEntity;
import com.oriole.wisepen.media.domain.entity.MediaWatermarkSessionEntity;
import com.oriole.wisepen.media.exception.MediaError;
import com.oriole.wisepen.media.provider.MediaWatermarkProvider;
import com.oriole.wisepen.media.repository.MediaInfoRepository;
import com.oriole.wisepen.media.repository.MediaWatermarkSessionRepository;
import com.oriole.wisepen.media.service.IMediaPlaybackService;
import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaPlaybackServiceImpl implements IMediaPlaybackService {

    private static final DateTimeFormatter WATERMARK_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final CopyOptions IGNORE_NULL_COPY_OPTIONS = CopyOptions.create().ignoreNullValue();
    private static final int MANIFEST_CACHE_WAIT_ATTEMPTS = 10;
    private static final long MANIFEST_CACHE_WAIT_MILLIS = 100L;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final MediaInfoRepository mediaInfoRepository;
    private final MediaWatermarkSessionRepository watermarkSessionRepository;
    private final RemoteStorageService remoteStorageService;
    private final MediaWatermarkProvider mediaWatermarkProvider;
    private final MediaProperties mediaProperties;
    private final RedisCacheManager redisCacheManager;

    @Override
    public MediaPlaybackSessionResponse createPlaybackSession(String resourceId, Long viewerId) {
        MediaInfoEntity mediaInfo = mediaInfoRepository.findByResourceId(resourceId)
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_NOT_FOUND));
        if (mediaInfo.getMediaStatus() == null || mediaInfo.getMediaStatus().getStatus() != MediaStatusEnum.READY) {
            throw new ServiceException(MediaError.MEDIA_PREVIEW_NOT_READY);
        }
        if (mediaInfo.getResourceType() == ResourceType.AUDIO) {
            return MediaPlaybackSessionResponse.builder()
                    .status(WatermarkSessionStatus.READY)
                    .deliveryMode(MediaDeliveryMode.AUDIO_SOURCE)
                    .forensicStatus(ForensicStatus.UNAVAILABLE)
                    .playbackUrl(remoteStorageService.getDownloadUrl(mediaInfo.getSourceObjectKey(), null).getData())
                    .build();
        }

        LocalDateTime accessedAt = LocalDateTime.now();
        String sessionId = IdUtil.fastSimpleUUID();
        String wmId = IdUtil.fastSimpleUUID();
        WatermarkPurpose purpose = mediaInfo.getResourceType() == ResourceType.IMAGE
                ? WatermarkPurpose.PREVIEW : WatermarkPurpose.PLAYBACK;
        MediaDeliveryMode deliveryMode = mediaInfo.getResourceType() == ResourceType.IMAGE
                ? MediaDeliveryMode.IMAGE_PREVIEW : MediaDeliveryMode.VIDEO_JIT_HLS;

        MediaWatermarkSessionEntity session = MediaWatermarkSessionEntity.builder()
                .sessionId(sessionId)
                .wmId(wmId)
                .viewerId(viewerId)
                .resourceId(resourceId)
                .mediaId(mediaInfo.getMediaId())
                .purpose(purpose)
                .accessedAt(accessedAt)
                .expiresAt(accessedAt.plusMinutes(mediaProperties.getSessionTtlMinutes()))
                .watermarkText(viewerId + " " + accessedAt.format(WATERMARK_TIME_FORMAT) + " " + mediaProperties.getAcademicUseText())
                .deliveryMode(deliveryMode)
                .status(WatermarkSessionStatus.PREPARING)
                .forensicStatus(ForensicStatus.PREPARING)
                .build();
        watermarkSessionRepository.save(session);

        // 会话先落库再调用 provider，确保后续泄露检测可以用 wmId 反查 viewer/resource/session。
        MediaPlaybackGrant grant = mediaWatermarkProvider.createPlaybackGrant(mediaInfo, session);
        // VIEW 权限不等于允许看源文件；暗水印不可用时默认 fail closed，除非产品显式开启降级预览。
        if (grant.getForensicStatus() == ForensicStatus.UNAVAILABLE && !mediaProperties.isAllowForensicUnavailablePreview()) {
            BeanUtil.copyProperties(MediaWatermarkSessionEntity.builder()
                    .status(WatermarkSessionStatus.FAILED)
                    .forensicStatus(ForensicStatus.UNAVAILABLE)
                    .build(), session, IGNORE_NULL_COPY_OPTIONS);
            watermarkSessionRepository.save(session);
            throw new ServiceException(MediaError.MEDIA_FORENSIC_UNAVAILABLE);
        }
        BeanUtil.copyProperties(MediaWatermarkSessionEntity.builder()
                .status(grant.getStatus())
                .deliveryMode(grant.getDeliveryMode())
                .forensicStatus(grant.getForensicStatus())
                .previewObjectKey(grant.getPreviewObjectKey())
                .manifestObjectKey(grant.getManifestObjectKey())
                .deliveryObjectKeys(grant.getDeliveryObjectKeys())
                .build(), session, IGNORE_NULL_COPY_OPTIONS);
        watermarkSessionRepository.save(session);
        return buildPlaybackSessionResponse(session, grant.getRetryAfterMs());
    }

    @Override
    public MediaPlaybackSessionResponse getPlaybackSession(String sessionId, Long viewerId) {
        MediaWatermarkSessionEntity session = watermarkSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_PLAYBACK_SESSION_NOT_FOUND));
        if (!viewerId.equals(session.getViewerId()) || session.getExpiresAt() == null || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ServiceException(MediaError.MEDIA_PLAYBACK_SESSION_NOT_FOUND);
        }
        return buildPlaybackSessionResponse(session, null);
    }

    @Override
    public String getPlaybackManifest(String sessionId, Long viewerId) {
        MediaWatermarkSessionEntity session = watermarkSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_PLAYBACK_SESSION_NOT_FOUND));
        if (!viewerId.equals(session.getViewerId()) || session.getExpiresAt() == null || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ServiceException(MediaError.MEDIA_PLAYBACK_SESSION_NOT_FOUND);
        }
        if (session.getDeliveryMode() != MediaDeliveryMode.VIDEO_JIT_HLS && session.getDeliveryMode() != MediaDeliveryMode.VIDEO_AB_HLS) {
            throw new ServiceException(MediaError.MEDIA_PLAYBACK_SESSION_NOT_FOUND);
        }
        if (session.getStatus() != WatermarkSessionStatus.READY && session.getStatus() != WatermarkSessionStatus.FINISHED) {
            throw new ServiceException(MediaError.MEDIA_PREVIEW_NOT_READY);
        }
        MediaInfoEntity mediaInfo = mediaInfoRepository.findById(session.getMediaId())
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_NOT_FOUND));
        if (mediaInfo.getMediaStatus() == null || mediaInfo.getMediaStatus().getStatus() != MediaStatusEnum.READY) {
            throw new ServiceException(MediaError.MEDIA_PREVIEW_NOT_READY);
        }
        if (StrUtil.isBlank(session.getManifestObjectKey())) {
            throw new ServiceException(MediaError.MEDIA_PREVIEW_NOT_READY);
        }
        try {
            return getCachedOrBuildPlaybackManifest(session);
        } catch (Exception e) {
            log.warn("media playback manifest build failed. resourceId={} mediaId={}",
                    session.getResourceId(), session.getMediaId(), e);
            throw new ServiceException(MediaError.MEDIA_PLAYBACK_FAILED, e.getMessage());
        }
    }

    private String getCachedOrBuildPlaybackManifest(MediaWatermarkSessionEntity session) throws Exception {
        String cachedManifest = readPlaybackManifestCache(session);
        if (cachedManifest != null) {
            return cachedManifest;
        }

        String lockToken = IdUtil.fastSimpleUUID();
        Boolean lockResult = tryLockPlaybackManifestBuild(session, lockToken);
        boolean lockAcquired = Boolean.TRUE.equals(lockResult);
        try {
            if (lockAcquired) {
                cachedManifest = readPlaybackManifestCache(session);
                if (cachedManifest != null) {
                    return cachedManifest;
                }
            } else if (Boolean.FALSE.equals(lockResult)) {
                cachedManifest = waitForCachedPlaybackManifest(session);
                if (cachedManifest != null) {
                    return cachedManifest;
                }
            }

            String manifest = buildPlaybackManifest(session);
            writePlaybackManifestCache(session, manifest);
            return manifest;
        } finally {
            if (lockAcquired) {
                unlockPlaybackManifestBuild(session, lockToken);
            }
        }
    }

    private String readPlaybackManifestCache(MediaWatermarkSessionEntity session) {
        try {
            String manifest = redisCacheManager.getPlaybackManifest(session.getSessionId());
            return StrUtil.isBlank(manifest) ? null : manifest;
        } catch (Exception e) {
            log.warn("media playback manifest cache read failed. resourceId={} mediaId={}",
                    session.getResourceId(), session.getMediaId(), e);
            return null;
        }
    }

    private Boolean tryLockPlaybackManifestBuild(MediaWatermarkSessionEntity session, String lockToken) {
        try {
            return redisCacheManager.tryLockPlaybackManifestBuild(
                    session.getSessionId(),
                    lockToken,
                    mediaProperties.getPlaybackManifestBuildLockTtlSeconds());
        } catch (Exception e) {
            log.warn("media playback manifest cache lock failed. resourceId={} mediaId={}",
                    session.getResourceId(), session.getMediaId(), e);
            return null;
        }
    }

    private String waitForCachedPlaybackManifest(MediaWatermarkSessionEntity session) {
        for (int i = 0; i < MANIFEST_CACHE_WAIT_ATTEMPTS; i++) {
            try {
                Thread.sleep(MANIFEST_CACHE_WAIT_MILLIS);
                String manifest = redisCacheManager.getPlaybackManifest(session.getSessionId());
                if (StrUtil.isNotBlank(manifest)) {
                    return manifest;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                log.warn("media playback manifest cache wait failed. resourceId={} mediaId={}",
                        session.getResourceId(), session.getMediaId(), e);
                return null;
            }
        }
        return null;
    }

    private void writePlaybackManifestCache(MediaWatermarkSessionEntity session, String manifest) {
        long ttlSeconds = resolvePlaybackManifestCacheTtlSeconds(session);
        if (ttlSeconds <= 0) {
            return;
        }
        try {
            redisCacheManager.setPlaybackManifest(session.getSessionId(), manifest, ttlSeconds);
        } catch (Exception e) {
            log.warn("media playback manifest cache write failed. resourceId={} mediaId={}",
                    session.getResourceId(), session.getMediaId(), e);
        }
    }

    private void unlockPlaybackManifestBuild(MediaWatermarkSessionEntity session, String lockToken) {
        try {
            redisCacheManager.unlockPlaybackManifestBuild(session.getSessionId(), lockToken);
        } catch (Exception e) {
            log.warn("media playback manifest cache unlock failed. resourceId={} mediaId={}",
                    session.getResourceId(), session.getMediaId(), e);
        }
    }

    private long resolvePlaybackManifestCacheTtlSeconds(MediaWatermarkSessionEntity session) {
        long sessionRemainingSeconds = Duration.between(LocalDateTime.now(), session.getExpiresAt()).getSeconds();
        long segmentUrlCacheSeconds = mediaProperties.getHlsSegmentUrlTtlSeconds()
                - mediaProperties.getPlaybackManifestCacheSafetySeconds();
        return Math.min(sessionRemainingSeconds, segmentUrlCacheSeconds);
    }

    private String buildPlaybackManifest(MediaWatermarkSessionEntity session) throws Exception {
        // 播放器拿到的是会话 manifest；这里把会话 HLS 中的 segment 相对路径改写为短时下载 URL。
        String manifestObjectKey = session.getManifestObjectKey();
        String manifestUrl = remoteStorageService.getDownloadUrl(manifestObjectKey, null).getData();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(manifestUrl)).GET().build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("媒体 HLS manifest 下载失败 StatusCode=" + response.statusCode());
        }
        return rewritePlaybackManifest(manifestObjectKey, response.body());
    }

    private String rewritePlaybackManifest(String manifestObjectKey, String manifest) {
        StringBuilder builder = new StringBuilder();
        int lastSlash = manifestObjectKey.lastIndexOf('/');
        String segmentPrefix = lastSlash >= 0 ? manifestObjectKey.substring(0, lastSlash) : "";
        Map<String, String> segmentUrls = new HashMap<>();
        for (String line : manifest.split("\\R", -1)) {
            if (StrUtil.isBlank(line) || line.startsWith("#") || line.startsWith("http://") || line.startsWith("https://")) {
                builder.append(line).append('\n');
                continue;
            }
            String cleanLine = line.replace('\\', '/').replaceAll("^/+", "");
            String segmentObjectKey = StrUtil.isBlank(segmentPrefix) ? cleanLine : segmentPrefix + "/" + cleanLine;
            String segmentUrl = segmentUrls.computeIfAbsent(segmentObjectKey,
                    key -> remoteStorageService.getDownloadUrl(key, mediaProperties.getHlsSegmentUrlTtlSeconds()).getData());
            builder.append(segmentUrl).append('\n');
        }
        return builder.toString();
    }

    private MediaPlaybackSessionResponse buildPlaybackSessionResponse(MediaWatermarkSessionEntity session, Long retryAfterMs) {
        MediaPlaybackSessionResponse.MediaPlaybackSessionResponseBuilder builder = MediaPlaybackSessionResponse.builder()
                .sessionId(session.getSessionId())
                .status(session.getStatus())
                .deliveryMode(session.getDeliveryMode())
                .forensicStatus(session.getForensicStatus())
                .watermarkText(session.getWatermarkText())
                .retryAfterMs(retryAfterMs);
        boolean ready = session.getStatus() == WatermarkSessionStatus.READY
                || session.getStatus() == WatermarkSessionStatus.FINISHED;
        if (ready && session.getDeliveryMode() == MediaDeliveryMode.IMAGE_PREVIEW
                && StrUtil.isNotBlank(session.getPreviewObjectKey())) {
            builder.previewUrl(remoteStorageService.getDownloadUrl(session.getPreviewObjectKey(), null).getData());
        }
        if (ready && (session.getDeliveryMode() == MediaDeliveryMode.VIDEO_JIT_HLS
                || session.getDeliveryMode() == MediaDeliveryMode.VIDEO_AB_HLS)
                && StrUtil.isNotBlank(session.getManifestObjectKey())) {
            builder.manifestUrl("/media/playback-sessions/" + session.getSessionId() + "/index.m3u8");
        }
        return builder.build();
    }
}
