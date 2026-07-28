package com.oriole.wisepen.media.api.domain.mq;

import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 媒体处理任务消息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaProcessTaskMessage implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 待处理的媒体记录 ID。 */
    private String mediaId;

    /** 源文件在 OSS 中的 ObjectKey。 */
    private String sourceObjectKey;

    /** 源文件对应的资源类型。 */
    private ResourceType resourceType;

    /** 源文件扩展名，用于本地处理时生成临时文件后缀。 */
    private String extension;
}
