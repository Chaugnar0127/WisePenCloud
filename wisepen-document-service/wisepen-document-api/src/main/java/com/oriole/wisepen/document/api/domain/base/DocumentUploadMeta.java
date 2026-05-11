package com.oriole.wisepen.document.api.domain.base;

import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadMeta {
    private String documentName;
    private Long uploaderId;
    /** 原始作者 ID，fork 链中始终保留最初上传者，不会被覆盖 */
    private Long originalAuthorId;
    private ResourceType fileType;
    private Long size;
}
