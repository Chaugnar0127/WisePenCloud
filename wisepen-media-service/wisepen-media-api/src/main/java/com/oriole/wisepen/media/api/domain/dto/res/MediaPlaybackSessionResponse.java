package com.oriole.wisepen.media.api.domain.dto.res;

import com.oriole.wisepen.media.api.enums.ForensicStatus;
import com.oriole.wisepen.media.api.enums.MediaDeliveryMode;
import com.oriole.wisepen.media.api.enums.WatermarkSessionStatus;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 媒体预览或播放会话响应。
 */
@Data
@Builder
public class MediaPlaybackSessionResponse implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 水印播放会话 ID；音频源文件播放授权可能为空。 */
    private String sessionId;

    /** 水印播放会话状态。 */
    private WatermarkSessionStatus status;

    /** 当前播放地址的交付模式。 */
    private MediaDeliveryMode deliveryMode;

    /** 暗水印取证能力状态。 */
    private ForensicStatus forensicStatus;

    /** 图片预览 URL。 */
    private String previewUrl;

    /** 视频 HLS manifest URL。 */
    private String manifestUrl;

    /** 音频源文件播放 URL。 */
    private String playbackUrl;

    /** 展示或嵌入到产物中的明水印文本。 */
    private String watermarkText;

    /** 会话还在准备中时建议客户端再次轮询的等待时间，单位毫秒。 */
    private Long retryAfterMs;
}
