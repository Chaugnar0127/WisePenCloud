package com.oriole.wisepen.resource.domain.dto;

import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceItemInfoResDTO {
    private String resourceId;
    private String resourceName;
    private ResourceType resourceType;
    private String ownerId;
    private String preview;
    private Long size;
}
