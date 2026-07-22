package com.oriole.wisepen.media.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.media.api.constant.MediaValidationMsg;
import com.oriole.wisepen.media.api.domain.base.MediaStatus;
import com.oriole.wisepen.media.api.domain.dto.req.MediaPlaybackSessionCreateRequest;
import com.oriole.wisepen.media.api.domain.dto.req.MediaUploadInitRequest;
import com.oriole.wisepen.media.api.domain.dto.req.MediaWatermarkDownloadCreateRequest;
import com.oriole.wisepen.media.api.domain.dto.res.MediaDownloadJobResponse;
import com.oriole.wisepen.media.api.domain.dto.res.MediaInfoResponse;
import com.oriole.wisepen.media.api.domain.dto.res.MediaPlaybackSessionResponse;
import com.oriole.wisepen.media.api.domain.dto.res.MediaUploadInitResponse;
import com.oriole.wisepen.media.exception.MediaError;
import com.oriole.wisepen.media.service.IMediaDownloadService;
import com.oriole.wisepen.media.service.IMediaPlaybackService;
import com.oriole.wisepen.media.service.IMediaService;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import com.oriole.wisepen.resource.enums.ResourceAction;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "媒体处理", description = "图片、视频和音频上传、处理状态、播放会话与下载")
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@CheckLogin
@Validated
public class MediaController {

    private final IMediaService mediaService;
    private final IMediaPlaybackService mediaPlaybackService;
    private final IMediaDownloadService mediaDownloadService;
    private final RemoteResourceService remoteResourceService;

    @Operation(
            summary = "初始化媒体上传",
            description = """
                    - 用途：为当前用户创建图片、视频或音频上传任务，并申请对象存储直传凭证。
                    - 请求：filename 为展示名；extension 为文件扩展名；md5 用于秒传判定；expectedSize 为预期大小。
                    - 约束：当前用户必须已登录；扩展名必须属于媒体服务支持的图片、视频或音频类型；一次上传生成一个新的媒体资源。
                    - 处理：创建待处理媒体记录，向存储服务申请上传 URL；上传完成或秒传后异步进入媒体处理与资源注册。
                    - 失败：文件类型不支持 -> MediaError.CANNOT_SUPPORT_FILE_TYPE；存储服务申请上传凭证失败 -> MediaError.MEDIA_UPLOAD_URL_APPLY_FAILED。
                    - 响应：返回 mediaId、objectKey、上传凭证信息和是否秒传。
                    """
    )
    @Log(title = "初始化媒体上传", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public R<MediaUploadInitResponse> upload(@Valid @RequestBody MediaUploadInitRequest request) {
        return R.ok(mediaService.initUploadMedia(request, SecurityContextHolder.getUserId()));
    }

    @Operation(
            summary = "查询未就绪媒体",
            description = """
                    - 用途：查询当前用户仍处于上传、处理、注册或失败状态的媒体任务。
                    - 请求：无显式请求参数，上传者来自当前登录上下文。
                    - 约束：当前用户必须已登录。
                    - 处理：按当前用户筛选非 READY 的待处理媒体；不触发重试。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN。
                    - 响应：返回未就绪媒体基础信息列表。
                    """
    )
    @GetMapping("/pending")
    public R<List<MediaInfoResponse>> listPendingMedia() {
        return R.ok(mediaService.listPendingMedia(SecurityContextHolder.getUserId()));
    }

    @Operation(
            summary = "刷新媒体状态",
            description = """
                    - 用途：主动同步当前用户上传媒体的最新处理状态。
                    - 请求：mediaId 指定待刷新的媒体任务。
                    - 约束：当前用户必须是该媒体上传者；目标媒体必须存在。
                    - 处理：如果媒体仍处于 UPLOADING，会查询文件存储记录；确认上传完成后更新为 UPLOADED 并发布处理任务。非 UPLOADING 状态仅返回当前状态。
                    - 失败：媒体不存在 -> MediaError.MEDIA_NOT_FOUND；当前用户不是上传者 -> MediaError.MEDIA_PERMISSION_DENIED；存储状态查询失败 -> MediaError.MEDIA_STORAGE_STATUS_GET_FAILED。
                    - 响应：返回刷新后的媒体状态。
                    """
    )
    @PostMapping("/sync-status")
    public R<MediaStatus> syncStatus(@RequestParam @NotBlank(message = MediaValidationMsg.MEDIA_ID_EMPTY) String mediaId) {
        mediaService.assertMediaUploader(mediaId, SecurityContextHolder.getUserId());
        return R.ok(mediaService.refreshMediaStatus(mediaId));
    }

    @Operation(
            summary = "重试媒体处理",
            description = """
                    - 用途：让上传者重新推进失败或资源注册超时的媒体处理任务。
                    - 请求：mediaId 指定待重试媒体。
                    - 约束：当前用户必须是该媒体上传者；媒体状态只能是 FAILED 或 REGISTERING_RES_TIMEOUT。
                    - 处理：FAILED 状态会重置为 UPLOADED 并重新发布处理任务；REGISTERING_RES_TIMEOUT 状态会重新执行资源注册完成流程。
                    - 失败：媒体不存在 -> MediaError.MEDIA_NOT_FOUND；当前用户不是上传者 -> MediaError.MEDIA_PERMISSION_DENIED；媒体状态不允许重试 -> MediaError.CANNOT_RETRY_MEDIA_PROCESS_IN_CURRENT_STATE；资源注册失败 -> MediaError.MEDIA_REGISTER_RESOURCE_FAILED。
                    - 响应：成功时返回空结果。
                    """
    )
    @Log(title = "重试媒体处理", businessType = BusinessType.UPDATE)
    @PostMapping("/retry-process")
    public R<Void> retryMediaProcess(@RequestParam @NotBlank(message = MediaValidationMsg.MEDIA_ID_EMPTY) String mediaId) {
        mediaService.assertMediaUploader(mediaId, SecurityContextHolder.getUserId());
        mediaService.retryMediaProcess(mediaId);
        return R.ok();
    }

    @Operation(
            summary = "创建媒体播放会话",
            description = """
                    - 用途：为有查看权限的用户创建图片预览、视频 HLS 播放会话或音频播放授权。
                    - 请求：resourceId 指定媒体资源。
                    - 约束：当前用户必须拥有 VIEW 动作；媒体必须已经处理完成；图片和视频暗水印 Provider 不可用时默认拒绝返回源文件；音频不需要水印。
                    - 处理：先通过资源服务校验 VIEW 权限；图片和视频创建带 wmId 的水印会话并交由水印 Provider 生成交付地址；音频直接申请源文件短时播放 URL，不创建水印会话。
                    - 失败：无查看权限 -> MediaError.MEDIA_PERMISSION_DENIED；媒体未就绪 -> MediaError.MEDIA_PREVIEW_NOT_READY；图片或视频暗水印能力不可用 -> MediaError.MEDIA_FORENSIC_UNAVAILABLE。
                    - 响应：图片和视频返回会话 ID、交付模式、明水印文本、预览 URL 或 HLS manifest URL；音频返回 AUDIO_SOURCE 交付模式和 playbackUrl。
                    """
    )
    @PostMapping("/playback-sessions")
    public R<MediaPlaybackSessionResponse> createPlaybackSession(
            @Valid @RequestBody MediaPlaybackSessionCreateRequest request) {
        assertResourceAction(request.getResourceId(), ResourceAction.VIEW);
        return R.ok(mediaPlaybackService.createPlaybackSession(request.getResourceId(), SecurityContextHolder.getUserId()));
    }

    @Operation(
            summary = "查询媒体播放会话",
            description = """
                    - 用途：查询当前用户已有图片预览或视频播放会话的生成结果；音频播放授权不创建可轮询会话。
                    - 请求：sessionId 指定播放会话。
                    - 约束：只能查询当前登录用户自己的未过期会话。
                    - 处理：读取会话状态和交付地址，不重新创建水印。
                    - 失败：会话不存在、不是本人会话或已过期 -> MediaError.MEDIA_PLAYBACK_SESSION_NOT_FOUND。
                    - 响应：返回会话状态、交付模式、明水印文本、预览 URL 或 HLS manifest URL。
                    """
    )
    @GetMapping("/playback-sessions/{sessionId}")
    public R<MediaPlaybackSessionResponse> getPlaybackSession(
            @PathVariable @NotBlank(message = MediaValidationMsg.SESSION_ID_EMPTY) String sessionId) {
        return R.ok(mediaPlaybackService.getPlaybackSession(sessionId, SecurityContextHolder.getUserId()));
    }

    @Operation(
            summary = "获取媒体播放会话 HLS manifest",
            description = """
                    - 用途：为视频播放会话返回当前可播放的 HLS manifest。
                    - 请求：sessionId 指定播放会话。
                    - 约束：只能访问当前登录用户自己的未过期视频会话；READY 前不返回可播放 manifest。
                    - 处理：读取源或会话 manifest，并将 segment URI 改写为短时防盗链 URL。
                    - 失败：会话不存在、不是本人会话或已过期 -> MediaError.MEDIA_PLAYBACK_SESSION_NOT_FOUND；媒体未就绪 -> MediaError.MEDIA_PREVIEW_NOT_READY。
                    - 响应：返回 application/vnd.apple.mpegurl 文本。
                    """
    )
    @GetMapping(value = "/playback-sessions/{sessionId}/index.m3u8", produces = "application/vnd.apple.mpegurl")
    public String getPlaybackManifest(
            @PathVariable @NotBlank(message = MediaValidationMsg.SESSION_ID_EMPTY) String sessionId) {
        return mediaPlaybackService.getPlaybackManifest(sessionId, SecurityContextHolder.getUserId());
    }

    @Operation(
            summary = "创建带水印下载任务",
            description = """
                    - 用途：为有带水印下载权限的用户创建媒体下载产物生成任务。
                    - 请求：resourceId 指定媒体资源。
                    - 约束：当前用户必须拥有 DOWNLOAD_WATERMARK 动作；媒体必须已经处理完成；音频不支持带水印下载；暗水印 Provider 不可用时任务会失败。
                    - 处理：先通过资源服务校验 DOWNLOAD_WATERMARK 权限，再创建下载会话和异步任务。
                    - 失败：无带水印下载权限 -> MediaError.MEDIA_PERMISSION_DENIED；媒体未就绪 -> MediaError.MEDIA_PREVIEW_NOT_READY；音频不支持带水印下载 -> MediaError.MEDIA_WATERMARK_NOT_SUPPORTED。
                    - 响应：返回下载任务 ID、任务状态和过期时间。
                    """
    )
    @Log(title = "创建媒体带水印下载任务", businessType = BusinessType.INSERT)
    @PostMapping("/download-jobs/watermark")
    public R<MediaDownloadJobResponse> createWatermarkDownloadJob(
            @Valid @RequestBody MediaWatermarkDownloadCreateRequest request) {
        assertResourceAction(request.getResourceId(), ResourceAction.DOWNLOAD_WATERMARK);
        return R.ok(mediaDownloadService.createWatermarkDownloadJob(request.getResourceId(), SecurityContextHolder.getUserId()));
    }

    @Operation(
            summary = "查询带水印下载任务",
            description = """
                    - 用途：查询当前用户创建的带水印下载任务进度和最终下载地址。
                    - 请求：jobId 指定下载任务。
                    - 约束：只能查询当前登录用户自己的任务。
                    - 处理：任务 READY 且产物存在时，向存储服务申请防盗链下载 URL。
                    - 失败：任务不存在或不是本人任务 -> MediaError.MEDIA_DOWNLOAD_JOB_NOT_FOUND。
                    - 响应：返回任务状态、失败原因或下载 URL。
                    """
    )
    @GetMapping("/download-jobs/{jobId}")
    public R<MediaDownloadJobResponse> getDownloadJob(
            @PathVariable @NotBlank(message = MediaValidationMsg.JOB_ID_EMPTY) String jobId) {
        return R.ok(mediaDownloadService.getDownloadJob(jobId, SecurityContextHolder.getUserId()));
    }

    @Operation(
            summary = "获取原始媒体下载地址",
            description = """
                    - 用途：为有源文件下载权限的用户获取图片、视频或音频原始文件下载 URL。
                    - 请求：resourceId 指定媒体资源。
                    - 约束：当前用户必须拥有 DOWNLOAD_ORIGINAL 动作；媒体必须已经处理完成。
                    - 处理：通过资源服务校验 DOWNLOAD_ORIGINAL 权限后，向存储服务申请源文件下载 URL。
                    - 失败：无源文件下载权限 -> MediaError.MEDIA_PERMISSION_DENIED；媒体未就绪 -> MediaError.MEDIA_PREVIEW_NOT_READY。
                    - 响应：返回短期可用的防盗链下载 URL。
                    """
    )
    @GetMapping("/download-original")
    public R<String> getOriginalDownloadUrl(
            @RequestParam @NotBlank(message = MediaValidationMsg.RESOURCE_ID_EMPTY) String resourceId) {
        assertResourceAction(resourceId, ResourceAction.DOWNLOAD_ORIGINAL);
        return R.ok(mediaDownloadService.getOriginalDownloadUrl(resourceId));
    }

    @Operation(
            summary = "获取媒体信息",
            description = """
                    - 用途：获取媒体资源详情和媒体处理信息，用于媒体详情页展示。
                    - 请求：resourceId 指定媒体资源。
                    - 约束：当前用户必须通过资源服务的资源详情权限校验；目标媒体信息必须存在。
                    - 处理：通过资源服务获取资源详情并校验访问权限，再读取媒体处理信息。
                    - 失败：资源无访问权限 -> ResourceError.RESOURCE_PERMISSION_DENIED；媒体不存在 -> MediaError.MEDIA_NOT_FOUND。
                    - 响应：返回媒体处理信息；资源详情由资源服务负责裁决。
                    """
    )
    @GetMapping("/info")
    public R<MediaInfoResponse> getMediaInfo(
            @RequestParam @NotBlank(message = MediaValidationMsg.RESOURCE_ID_EMPTY) String resourceId) {
        assertResourceAction(resourceId, ResourceAction.VIEW);
        return R.ok(mediaService.getMediaInfo(resourceId));
    }

    private void assertResourceAction(String resourceId, ResourceAction action) {
        ResourceCheckPermissionResDTO permission = remoteResourceService.checkResPermission(ResourceCheckPermissionReqDTO.builder()
                .resourceId(resourceId)
                .userId(SecurityContextHolder.getUserId())
                .groupRoles(SecurityContextHolder.getGroupRoleMap())
                .build()).getData();
        if (permission == null || permission.getAllowedActions() == null || !permission.getAllowedActions().contains(action)) {
            throw new ServiceException(MediaError.MEDIA_PERMISSION_DENIED);
        }
    }
}
