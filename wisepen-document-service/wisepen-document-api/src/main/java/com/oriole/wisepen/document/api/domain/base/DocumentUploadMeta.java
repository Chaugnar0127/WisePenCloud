package com.oriole.wisepen.document.api.domain.base;

import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadMeta {
    private String documentName;
    private Long uploaderId;
    private ResourceType fileType;
    private Long size;
    private String pathTagId;
    private Map<Long, GroupRoleType> groupRoles;
}
