package com.oriole.wisepen.media.api.domain.dto.res;

import com.oriole.wisepen.media.api.domain.base.MediaStatus;
import com.oriole.wisepen.media.api.domain.base.MediaUploadMeta;
import com.oriole.wisepen.media.api.enums.ForensicCapability;
import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 媒体资源处理信息。
 */
@Data
public class MediaInfoResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String mediaId;
    private String resourceId;
    private ResourceType resourceType;
    private String sourceObjectKey;
    private String sourceHlsPrefix;
    private String previewObjectKey;
    private String posterObjectKey;
    private Long durationMs;
    private Integer width;
    private Integer height;
    private Long size;
    private MediaStatus mediaStatus;
    private ForensicCapability forensicCapability;
    private MediaUploadMeta uploadMeta;
}
