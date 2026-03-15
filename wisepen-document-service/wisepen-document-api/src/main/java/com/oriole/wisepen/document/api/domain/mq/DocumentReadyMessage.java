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

    /** 文档唯一 ID（即 resource 服务的 resourceId） */
    private String documentId;

    /** 生成的 PDF 预览文件在 OSS 中的 ObjectKey */
    private String previewObjectKey;

    /** MongoDB 中纯文本文档的 _id */
    private String textMongoId;
}
