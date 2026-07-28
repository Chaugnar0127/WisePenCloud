package com.oriole.wisepen.media.api.domain.dto.req;

import com.oriole.wisepen.media.api.constant.MediaValidationMsg;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建媒体预览或播放会话请求。
 */
@Data
public class MediaPlaybackSessionCreateRequest implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 需要创建水印播放会话的资源 ID。 */
    @NotBlank(message = MediaValidationMsg.RESOURCE_ID_EMPTY)
    private String resourceId;
}
