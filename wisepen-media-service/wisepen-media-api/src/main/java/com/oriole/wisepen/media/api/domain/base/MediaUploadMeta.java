package com.oriole.wisepen.media.api.domain.base;

import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadMeta {
    private String mediaName;
    private Long uploaderId;
    private ResourceType resourceType;
    private String extension;
    private Long size;
}
