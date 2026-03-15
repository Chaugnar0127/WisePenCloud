package com.oriole.wisepen.document.api.domain.base;

import com.oriole.wisepen.document.api.enums.DocumentStatusEnum;
import lombok.Data;

/**
 * 文档状态机核心字段（仅存放文档服务自身处理流水线所需的最少状态）
 * <p>
 * 文件名、大小、MD5、上传者等元信息由 resource / storage 服务负责维护，
 * 本服务不重复存储，避免与上游服务争夺单一数据来源。
 * </p>
 */
@Data
public class DocumentInfoBase {

    /** 文档状态机 */
    private DocumentStatusEnum status;

    /** 原始文件在 OSS 中的 ObjectKey（由 storage 服务分配） */
    private String sourceObjectKey;

    /** PDF 预览文件在 OSS 中的 ObjectKey（Stage 3 归档后写入） */
    private String previewObjectKey;

    /** MongoDB 中纯文本文档的 _id（Stage 3 归档后写入） */
    private String textMongoId;
}
