package com.oriole.wisepen.media.api.domain.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短视频会话级 HLS JIT 任务消息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaJitPlaybackTaskMessage implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 需要执行 JIT 处理的水印播放会话 ID。 */
    private String sessionId;

    /** 会话关联的媒体记录 ID。 */
    private String mediaId;

    /** 会话关联的资源 ID。 */
    private String resourceId;
}
