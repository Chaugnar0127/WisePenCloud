package com.oriole.wisepen.media.api.domain.dto.res;

import com.oriole.wisepen.media.api.enums.MediaDeliveryMode;
import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 媒体无水印播放响应
 */
@Data
@Builder
public class MediaPlaybackResponse implements Serializable {

    /** 序列化版本号 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 被播放的资源 ID */
    private String resourceId;

    /** 媒体处理记录 ID */
    private String mediaId;

    /** 媒体资源类型 */
    private ResourceType resourceType;

    /** 播放交付模式 */
    private MediaDeliveryMode deliveryMode;

    /** 图片或视频封面图 URL */
    private String coverUrl;

    /** 视频源 HLS manifest URL */
    private String manifestUrl;

    /** 图片或音频源文件播放 URL */
    private String playbackUrl;

    /** 音频或视频时长，单位毫秒 */
    private Long durationMs;

    /** 图片或视频宽度，单位像素 */
    private Integer width;

    /** 图片或视频高度，单位像素 */
    private Integer height;
}
