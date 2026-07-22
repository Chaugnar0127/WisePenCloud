package com.oriole.wisepen.media.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitReqDTO;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitRespDTO;
import com.oriole.wisepen.file.storage.api.enums.StorageSceneEnum;
import com.oriole.wisepen.file.storage.api.feign.RemoteStorageService;
import com.oriole.wisepen.media.api.domain.base.MediaStatus;
import com.oriole.wisepen.media.api.domain.mq.MediaProcessTaskMessage;
import com.oriole.wisepen.media.api.domain.mq.MediaReadyMessage;
import com.oriole.wisepen.media.api.enums.ForensicCapability;
import com.oriole.wisepen.media.api.enums.MediaStatusEnum;
import com.oriole.wisepen.media.config.MediaProperties;
import com.oriole.wisepen.media.domain.MediaPackagingResult;
import com.oriole.wisepen.media.domain.entity.MediaInfoEntity;
import com.oriole.wisepen.media.exception.MediaError;
import com.oriole.wisepen.media.mq.KafkaMediaEventPublisher;
import com.oriole.wisepen.media.repository.MediaInfoRepository;
import com.oriole.wisepen.media.service.IMediaProcessService;
import com.oriole.wisepen.resource.domain.dto.ResourceCreateReqDTO;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaProcessServiceImpl implements IMediaProcessService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final MediaInfoRepository mediaInfoRepository;
    private final KafkaMediaEventPublisher eventPublisher;
    private final RemoteResourceService remoteResourceService;
    private final RemoteStorageService remoteStorageService;
    private final MediaProperties mediaProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void processMedia(MediaProcessTaskMessage message) {
        String mediaId = message.getMediaId();
        MediaInfoEntity entity = mediaInfoRepository.findById(mediaId)
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_NOT_FOUND));
        MediaStatusEnum status = entity.getMediaStatus() != null ? entity.getMediaStatus().getStatus() : null;

        if (status == MediaStatusEnum.REGISTERING_RES || status == MediaStatusEnum.REGISTERING_RES_TIMEOUT) {
            finalizeToReady(mediaId);
            return;
        }

        if (status != MediaStatusEnum.UPLOADED) {
            log.info("media process skipped because status mismatched. mediaId={} status={}",
                    entity.getMediaId(), status);
            return;
        }

        updateStatus(mediaId, new MediaStatus(MediaStatusEnum.PROBING));

        // packaging 只生成媒体基础产物：图片预览基准图、视频源 HLS 与封面；音频只读取音频流元数据。
        // 观看者级明/暗水印在 playback/download session 阶段处理，不能在上传阶段预埋。
        MediaPackagingResult packagingResult;
        if (entity.getResourceType() == ResourceType.IMAGE) {
            updateStatus(mediaId, new MediaStatus(MediaStatusEnum.PACKAGING));
            packagingResult = packageImage(entity);
        } else if (entity.getResourceType() == ResourceType.VIDEO) {
            updateStatus(mediaId, new MediaStatus(MediaStatusEnum.PACKAGING));
            packagingResult = packageVideo(entity);
        } else if (entity.getResourceType() == ResourceType.AUDIO) {
            packagingResult = packageAudio(entity);
        } else {
            throw new ServiceException(MediaError.CANNOT_SUPPORT_FILE_TYPE);
        }
        mediaInfoRepository.updatePackagingResultById(mediaId,
                packagingResult.getSourceHlsPrefix(),
                packagingResult.getSourceHlsObjectKeys(),
                packagingResult.getPreviewObjectKey(),
                packagingResult.getPosterObjectKey(),
                packagingResult.getDurationMs(),
                packagingResult.getWidth(),
                packagingResult.getHeight());

        if (entity.getResourceType() != ResourceType.AUDIO) {
            updateStatus(mediaId, new MediaStatus(MediaStatusEnum.FORENSIC_PREPROCESSING));
        }

        // 首期没有真实暗水印 provider 时必须显式记录不可用，音频则始终不进入水印链路。
        mediaInfoRepository.updateForensicCapabilityById(mediaId, ForensicCapability.UNAVAILABLE);

        finalizeToReady(mediaId);
    }

    @Override
    public void updateStatus(String mediaId, MediaStatus status) {
        MediaInfoEntity entity = mediaInfoRepository.findById(mediaId)
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_NOT_FOUND));
        MediaStatusEnum from = entity.getMediaStatus() != null ? entity.getMediaStatus().getStatus() : null;
        mediaInfoRepository.updateStatusById(mediaId, status);
        log.info("media status changed. mediaId={} resourceId={} from={} to={}",
                mediaId, entity.getResourceId(), from, status.getStatus());
    }

    @Override
    public void prepareProcessRetry(String mediaId) {
        MediaInfoEntity entity = mediaInfoRepository.findById(mediaId)
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_NOT_FOUND));
        MediaStatusEnum status = entity.getMediaStatus() != null ? entity.getMediaStatus().getStatus() : null;
        if (status == MediaStatusEnum.REGISTERING_RES
                || status == MediaStatusEnum.REGISTERING_RES_TIMEOUT
                || status == MediaStatusEnum.READY
                || status == MediaStatusEnum.FAILED) {
            return;
        }
        if (status == null
                || status == MediaStatusEnum.UPLOADING
                || status == MediaStatusEnum.UPLOADED
                || status == MediaStatusEnum.PROBING
                || status == MediaStatusEnum.PACKAGING
                || status == MediaStatusEnum.FORENSIC_PREPROCESSING) {
            if (status != MediaStatusEnum.UPLOADED) {
                updateStatus(mediaId, new MediaStatus(MediaStatusEnum.UPLOADED));
            }
        }
    }

    @Override
    public void markProcessFailed(String mediaId, String errorMessage) {
        MediaInfoEntity entity = mediaInfoRepository.findById(mediaId)
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_NOT_FOUND));
        MediaStatusEnum status = entity.getMediaStatus() != null ? entity.getMediaStatus().getStatus() : null;
        if (status == MediaStatusEnum.READY
                || status == MediaStatusEnum.FAILED
                || status == MediaStatusEnum.REGISTERING_RES_TIMEOUT) {
            return;
        }
        if (status == MediaStatusEnum.REGISTERING_RES) {
            updateStatus(mediaId, new MediaStatus(MediaStatusEnum.REGISTERING_RES_TIMEOUT, errorMessage));
            return;
        }
        updateStatus(mediaId, new MediaStatus(errorMessage));
    }

    private MediaPackagingResult packageImage(MediaInfoEntity mediaInfo) {
        Integer width = null;
        Integer height = null;
        File sourceFile = null;
        try {
            String downloadUrl = remoteStorageService.getDownloadUrl(mediaInfo.getSourceObjectKey(), null).getData();
            String sourceExtension;
            if (mediaInfo.getUploadMeta() != null && StrUtil.isNotBlank(mediaInfo.getUploadMeta().getExtension())) {
                sourceExtension = mediaInfo.getUploadMeta().getExtension();
            } else {
                sourceExtension = FileUtil.extName(mediaInfo.getSourceObjectKey());
            }
            sourceFile = downloadSourceFile(downloadUrl, mediaInfo.getMediaId(), sourceExtension);
            ImageSize imageSize = readImageSize(sourceFile);
            if (imageSize == null) {
                throw new ServiceException(MediaError.MEDIA_PROCESS_FAILED, "图片尺寸读取失败");
            }
            width = imageSize.width();
            height = imageSize.height();
        } catch (Exception e) {
            log.warn("media image probe failed. mediaId={} objectKey={}",
                    mediaInfo.getMediaId(), mediaInfo.getSourceObjectKey(), e);
            throw new ServiceException(MediaError.MEDIA_PROCESS_FAILED, e.getMessage());
        } finally {
            if (sourceFile != null) {
                Path path = sourceFile.toPath();
                try {
                    Files.deleteIfExists(path);
                } catch (Exception e) {
                    log.warn("media cache file delete failed. path={}", path, e);
                }
            }
        }

        return MediaPackagingResult.builder()
                .previewObjectKey(mediaInfo.getSourceObjectKey())
                .width(width)
                .height(height)
                .build();
    }

    private MediaPackagingResult packageVideo(MediaInfoEntity mediaInfo) {
        Path workDir = null;
        try {
            Path cacheRoot = Paths.get(mediaProperties.getCachePath());
            Files.createDirectories(cacheRoot);
            workDir = Files.createTempDirectory(cacheRoot, mediaInfo.getMediaId() + "_");

            String sourceUrl = remoteStorageService.getDownloadUrl(mediaInfo.getSourceObjectKey(), null).getData();
            String sourceExtension;
            if (mediaInfo.getUploadMeta() != null && StrUtil.isNotBlank(mediaInfo.getUploadMeta().getExtension())) {
                sourceExtension = mediaInfo.getUploadMeta().getExtension();
            } else {
                sourceExtension = FileUtil.extName(mediaInfo.getSourceObjectKey());
            }
            File sourceFile = downloadSourceFile(sourceUrl, mediaInfo.getMediaId(), sourceExtension);
            Path sourcePath = workDir.resolve(sourceFile.getName());
            Files.move(sourceFile.toPath(), sourcePath);

            VideoProbe probe = probeVideo(sourcePath);
            Path hlsDir = workDir.resolve("hls");
            Files.createDirectories(hlsDir);
            runFfmpegToHls(sourcePath, hlsDir);

            String hlsPrefix = StorageSceneEnum.PRIVATE_MEDIA.getPrefix()
                    + "/" + mediaInfo.getMediaId() + "/source-hls";
            List<String> hlsObjectKeys = new ArrayList<>();
            try (var stream = Files.list(hlsDir)) {
                for (Path file : stream.filter(Files::isRegularFile).toList()) {
                    hlsObjectKeys.add(uploadFile(file, hlsPrefix + "/" + file.getFileName(), mediaInfo.getMediaId()));
                }
            }

            String posterObjectKey = null;
            Path posterPath = workDir.resolve("poster.jpg");
            try {
                runFfmpegToPoster(sourcePath, posterPath);
                posterObjectKey = uploadFile(posterPath, hlsPrefix + "/poster.jpg", mediaInfo.getMediaId());
            } catch (Exception e) {
                log.warn("media poster generation failed. mediaId={}", mediaInfo.getMediaId(), e);
            }

            return MediaPackagingResult.builder()
                    .sourceHlsPrefix(hlsPrefix)
                    .sourceHlsObjectKeys(hlsObjectKeys)
                    .posterObjectKey(posterObjectKey)
                    .durationMs(probe.durationMs())
                    .width(probe.width())
                    .height(probe.height())
                    .build();
        } catch (Exception e) {
            log.warn("media video packaging failed. mediaId={} objectKey={}",
                    mediaInfo.getMediaId(), mediaInfo.getSourceObjectKey(), e);
            throw new ServiceException(MediaError.MEDIA_PROCESS_FAILED, e.getMessage());
        } finally {
            if (workDir != null) {
                try (var paths = Files.walk(workDir)) {
                    paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            log.warn("media cache file delete failed. path={}", path, e);
                        }
                    });
                } catch (Exception e) {
                    log.warn("media cache directory delete failed. path={}", workDir, e);
                }
            }
        }
    }

    private MediaPackagingResult packageAudio(MediaInfoEntity mediaInfo) {
        File sourceFile = null;
        try {
            String downloadUrl = remoteStorageService.getDownloadUrl(mediaInfo.getSourceObjectKey(), null).getData();
            String sourceExtension;
            if (mediaInfo.getUploadMeta() != null && StrUtil.isNotBlank(mediaInfo.getUploadMeta().getExtension())) {
                sourceExtension = mediaInfo.getUploadMeta().getExtension();
            } else {
                sourceExtension = FileUtil.extName(mediaInfo.getSourceObjectKey());
            }
            sourceFile = downloadSourceFile(downloadUrl, mediaInfo.getMediaId(), sourceExtension);
            AudioProbe probe = probeAudio(sourceFile.toPath());
            return MediaPackagingResult.builder()
                    .durationMs(probe.durationMs())
                    .build();
        } catch (Exception e) {
            log.warn("media audio probe failed. mediaId={} objectKey={}",
                    mediaInfo.getMediaId(), mediaInfo.getSourceObjectKey(), e);
            throw new ServiceException(MediaError.MEDIA_PROCESS_FAILED, e.getMessage());
        } finally {
            if (sourceFile != null) {
                Path path = sourceFile.toPath();
                try {
                    Files.deleteIfExists(path);
                } catch (Exception e) {
                    log.warn("media cache file delete failed. path={}", path, e);
                }
            }
        }
    }

    private File downloadSourceFile(String url, String mediaId, String extension) throws IOException, InterruptedException {
        Path dir = Paths.get(mediaProperties.getCachePath());
        Files.createDirectories(dir);
        Path target = Files.createTempFile(dir, mediaId + "_source_", "." + extension);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<Path> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofFile(target));
        if (response.statusCode() / 100 != 2) {
            Files.deleteIfExists(target);
            throw new IllegalStateException("媒体源文件下载失败 StatusCode=" + response.statusCode());
        }
        return target.toFile();
    }

    private ImageSize readImageSize(File file) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            if (input == null) {
                return null;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                BufferedImage image = ImageIO.read(file);
                return image == null ? null : new ImageSize(image.getWidth(), image.getHeight());
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                return new ImageSize(reader.getWidth(0), reader.getHeight(0));
            } finally {
                reader.dispose();
            }
        }
    }

    private VideoProbe probeVideo(Path sourcePath) throws IOException {
        String output = runCommand(List.of(
                mediaProperties.getFfprobePath(),
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height:format=duration",
                "-of", "json",
                sourcePath.toString()
        ), Duration.ofMillis(mediaProperties.getFfmpegTimeoutMs()));
        JsonNode root = objectMapper.readTree(output);
        JsonNode stream = root.path("streams").isArray() && !root.path("streams").isEmpty()
                ? root.path("streams").get(0) : objectMapper.createObjectNode();
        double durationSeconds = root.path("format").path("duration").asDouble(0D);
        return new VideoProbe(
                Math.round(durationSeconds * 1000D),
                stream.path("width").isMissingNode() ? null : stream.path("width").asInt(),
                stream.path("height").isMissingNode() ? null : stream.path("height").asInt()
        );
    }

    private AudioProbe probeAudio(Path sourcePath) throws IOException {
        String output = runCommand(List.of(
                mediaProperties.getFfprobePath(),
                "-v", "error",
                "-select_streams", "a:0",
                "-show_entries", "stream=codec_type:format=duration",
                "-of", "json",
                sourcePath.toString()
        ), Duration.ofMillis(mediaProperties.getFfmpegTimeoutMs()));
        JsonNode root = objectMapper.readTree(output);
        JsonNode streams = root.path("streams");
        if (!streams.isArray() || streams.isEmpty()) {
            throw new ServiceException(MediaError.MEDIA_PROCESS_FAILED, "音频流不存在");
        }
        double durationSeconds = root.path("format").path("duration").asDouble(0D);
        return new AudioProbe(Math.round(durationSeconds * 1000D));
    }

    private void runFfmpegToHls(Path sourcePath, Path hlsDir) {
        runCommand(List.of(
                mediaProperties.getFfmpegPath(),
                "-y",
                "-i", sourcePath.toString(),
                "-map", "0:v:0",
                "-map", "0:a?",
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-c:a", "aac",
                "-hls_time", String.valueOf(mediaProperties.getHlsSegmentSeconds()),
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", hlsDir.resolve("seg-%05d.ts").toString(),
                hlsDir.resolve("index.m3u8").toString()
        ), Duration.ofMillis(mediaProperties.getFfmpegTimeoutMs()));
    }

    private void runFfmpegToPoster(Path sourcePath, Path posterPath) {
        runCommand(List.of(
                mediaProperties.getFfmpegPath(),
                "-y",
                "-ss", "00:00:01",
                "-i", sourcePath.toString(),
                "-frames:v", "1",
                "-q:v", "2",
                posterPath.toString()
        ), Duration.ofMillis(mediaProperties.getFfmpegTimeoutMs()));
    }

    private String runCommand(List<String> command, Duration timeout) {
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    return e.getMessage();
                }
            });
            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("媒体处理命令超时");
            }
            String output = outputFuture.get(5, TimeUnit.SECONDS);
            if (process.exitValue() != 0) {
                throw new IllegalStateException(output);
            }
            return output;
        } catch (Exception e) {
            throw new ServiceException(MediaError.MEDIA_PROCESS_FAILED, e.getMessage());
        }
    }

    private String uploadFile(Path file, String objectKey, String mediaId) {
        try {
            String extension = FileUtil.extName(file.getFileName().toString());
            UploadInitRespDTO uploadInitResp = remoteStorageService.initUpload(UploadInitReqDTO.builder()
                    .md5(SecureUtil.md5(file.toFile()))
                    .extension(extension)
                    .scene(StorageSceneEnum.PRIVATE_MEDIA)
                    .bizTag(mediaId)
                    .targetObjectKey(objectKey)
                    .expectedSize(Files.size(file))
                    .isNeedCallback(false)
                    .build()).getData();
            if (!Boolean.TRUE.equals(uploadInitResp.getFlashUploaded())) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(uploadInitResp.getPutUrl()))
                        .header("Content-Type", "application/octet-stream")
                        .PUT(HttpRequest.BodyPublishers.ofFile(file))
                        .build();
                HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() / 100 != 2) {
                    throw new IllegalStateException("媒体产物上传失败 StatusCode=" + response.statusCode());
                }
            }
            return uploadInitResp.getObjectKey();
        } catch (Exception e) {
            throw new ServiceException(MediaError.MEDIA_PROCESS_FAILED, e.getMessage());
        }
    }

    @Override
    public void finalizeToReady(String mediaId) {
        MediaInfoEntity entity = mediaInfoRepository.findById(mediaId)
                .orElseThrow(() -> new ServiceException(MediaError.MEDIA_NOT_FOUND));
        if (entity.getMediaStatus() != null && entity.getMediaStatus().getStatus() == MediaStatusEnum.READY) {
            return;
        }

        updateStatus(mediaId, new MediaStatus(MediaStatusEnum.REGISTERING_RES));
        String resourceId = entity.getResourceId();
        if (StrUtil.isBlank(resourceId)) {
            try {
                // 只有基础产物和取证能力状态确定后才注册 resource，避免资源服务暴露未完成媒体。
                resourceId = remoteResourceService.createResource(ResourceCreateReqDTO.builder()
                        .resourceName(entity.getUploadMeta().getMediaName())
                        .resourceType(entity.getUploadMeta().getResourceType())
                        .ownerId(String.valueOf(entity.getOwnerId()))
                        .preview(entity.getPosterObjectKey() != null ? entity.getPosterObjectKey() : entity.getPreviewObjectKey())
                        .size(entity.getSize())
                        .build()).getData();
            } catch (Exception e) {
                log.error("media resource register failed. mediaId={}", mediaId, e);
                updateStatus(mediaId, new MediaStatus(MediaStatusEnum.REGISTERING_RES_TIMEOUT));
                throw new ServiceException(MediaError.MEDIA_REGISTER_RESOURCE_FAILED, e.getMessage());
            }
            mediaInfoRepository.updateResourceIdById(mediaId, resourceId);
        }

        updateStatus(mediaId, new MediaStatus(MediaStatusEnum.READY));
        eventPublisher.publishReadyEvent(MediaReadyMessage.builder()
                .resourceId(resourceId)
                .mediaId(mediaId)
                .resourceType(entity.getResourceType())
                .build());
        log.info("media ready finalized. mediaId={} resourceId={}", mediaId, resourceId);
    }

    private record ImageSize(Integer width, Integer height) {
    }

    private record VideoProbe(Long durationMs, Integer width, Integer height) {
    }

    private record AudioProbe(Long durationMs) {
    }
}
