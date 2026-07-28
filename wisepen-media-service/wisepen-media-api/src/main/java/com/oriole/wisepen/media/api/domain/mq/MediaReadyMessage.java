package com.oriole.wisepen.media.api.domain.mq;

import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 媒体处理就绪事件。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaReadyMessage implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 已就绪媒体对应的资源 ID。 */
    private String resourceId;

    /** 已就绪的媒体记录 ID。 */
    private String mediaId;

    /** 已就绪媒体的资源类型。 */
    private ResourceType resourceType;
}
