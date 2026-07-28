package com.oriole.wisepen.media.api.domain.dto.res;

import com.oriole.wisepen.media.api.domain.base.MediaStatus;
import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 媒体资源处理信息
 */
@Data
public class MediaInfoResponse implements Serializable {

    /** 序列化版本号 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 媒体处理记录 ID */
    private String mediaId;

    /** 资源服务中的资源 ID，媒体处理完成前可能为空 */
    private String resourceId;

    /** 媒体资源类型 */
    private ResourceType resourceType;

    /** 用户上传时携带的原始文件名 */
    private String originalFilename;

    /** 源文件扩展名 */
    private String sourceExtension;

    /** 源文件在 OSS 中的 ObjectKey */
    private String sourceObjectKey;

    /** 视频源 HLS 在 OSS 中的目录前缀 */
    private String sourceHlsPrefix;

    /** 图片预览图或视频封面图在 OSS 中的 ObjectKey */
    private String previewObjectKey;

    /** 音频或视频时长，单位毫秒 */
    private Long durationMs;

    /** 图片或视频宽度，单位像素 */
    private Integer width;

    /** 图片或视频高度，单位像素 */
    private Integer height;

    /** 源文件大小，单位字节 */
    private Long size;

    /** 当前媒体处理状态和失败原因 */
    private MediaStatus mediaStatus;
}
