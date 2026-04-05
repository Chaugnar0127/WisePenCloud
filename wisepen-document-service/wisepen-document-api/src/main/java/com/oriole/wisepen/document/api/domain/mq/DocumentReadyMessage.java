package com.oriole.wisepen.document.api.domain.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档处理就绪事件消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReadyMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String resourceId;
    private String content;
}
