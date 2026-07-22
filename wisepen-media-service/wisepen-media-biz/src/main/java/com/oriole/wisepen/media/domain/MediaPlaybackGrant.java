package com.oriole.wisepen.media.domain;

import com.oriole.wisepen.media.api.enums.ForensicStatus;
import com.oriole.wisepen.media.api.enums.MediaDeliveryMode;
import com.oriole.wisepen.media.api.enums.WatermarkSessionStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Provider 返回的播放授权内部结果。
 */
@Data
@Builder
public class MediaPlaybackGrant {

    private WatermarkSessionStatus status;

    private MediaDeliveryMode deliveryMode;

    private ForensicStatus forensicStatus;

    private String previewObjectKey;

    private String manifestObjectKey;

    private List<String> deliveryObjectKeys;

    private Long retryAfterMs;
}
