package com.oriole.wisepen.media.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 媒体基础处理产物结果
 */
@Data
@Builder
public class MediaPackagingResult {

    /** 视频源 HLS 在 OSS 中的目录前缀 */
    private String sourceHlsPrefix;

    /** 视频源 HLS 具体文件 ObjectKey 列表，用于资源删除时精确清理 */
    private List<String> sourceHlsObjectKeys;

    /** 图片预览图或视频封面图在 OSS 中的 ObjectKey */
    private String previewObjectKey;

    /** 音频或视频时长，单位毫秒 */
    private Long durationMs;

    /** 图片或视频宽度，单位像素 */
    private Integer width;

    /** 图片或视频高度，单位像素 */
    private Integer height;
}
