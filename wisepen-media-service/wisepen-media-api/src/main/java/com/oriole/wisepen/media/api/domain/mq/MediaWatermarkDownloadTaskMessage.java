package com.oriole.wisepen.media.api.domain.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 带水印下载产物生成任务消息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaWatermarkDownloadTaskMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String jobId;
    private String sessionId;
    private String mediaId;
    private String resourceId;
}
