package com.oriole.wisepen.document.api.domain.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersionBase {
    private DocumentUploadMeta uploadMeta;
    private DocumentStatus documentStatus;
    private Integer maxPreviewPages;
}
