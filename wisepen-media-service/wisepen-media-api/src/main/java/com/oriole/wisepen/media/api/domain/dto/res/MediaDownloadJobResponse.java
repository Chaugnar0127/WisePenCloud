package com.oriole.wisepen.media.api.domain.dto.res;

import com.oriole.wisepen.media.api.enums.MediaDownloadJobStatus;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 媒体带水印下载任务响应。
 */
@Data
@Builder
public class MediaDownloadJobResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String jobId;
    private String sessionId;
    private String resourceId;
    private String mediaId;
    private MediaDownloadJobStatus status;
    private String downloadUrl;
    private String failReason;
    private LocalDateTime expiresAt;
}
