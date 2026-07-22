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

    @Serial
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private WatermarkSessionStatus status;
    private MediaDeliveryMode deliveryMode;
    private ForensicStatus forensicStatus;
    private String previewUrl;
    private String manifestUrl;
    private String playbackUrl;
    private String watermarkText;
    private Long retryAfterMs;
}
