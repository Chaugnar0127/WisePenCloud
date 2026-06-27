package com.oriole.wisepen.document.domain.entity;

import com.oriole.wisepen.document.api.domain.base.DocumentVersionBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wisepen_document_versions")
@CompoundIndex(
        name = "idx_resource_version",
        def = "{'resourceId': 1, 'version': 1}",
        unique = true,
        partialFilter = "{'resourceId': {'$exists': true, '$ne': null}}"
)
public class DocumentVersionEntity extends DocumentVersionBase {

    @Id
    private String documentId;

    /** 关联的资源服务 ID (延迟绑定：初始化时为 null，解析成功后回写) */
    private String resourceId;

    private Integer version;

    /** 原始文件在 OSS 中的 ObjectKey（由 storage 服务分配） */
    private String sourceObjectKey;

    /** PDF 预览文件在 OSS 中的 ObjectKey（Stage 3 归档后写入） */
    private String previewObjectKey;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;
}
