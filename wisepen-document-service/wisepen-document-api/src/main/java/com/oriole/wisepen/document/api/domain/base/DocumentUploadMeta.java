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
    private ResourceType fileType;
    private Long size;
}
