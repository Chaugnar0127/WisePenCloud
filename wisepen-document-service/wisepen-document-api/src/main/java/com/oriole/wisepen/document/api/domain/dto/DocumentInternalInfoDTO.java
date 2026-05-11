package com.oriole.wisepen.document.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInternalInfoDTO {
    private String documentId;
    private String resourceId;
    private String sourceObjectKey;
    private String previewObjectKey;
}
