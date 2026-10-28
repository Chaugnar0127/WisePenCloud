package com.oriole.wisepen.web.app.domain.base;

import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleHtmlPageUploadMeta {
    private String filename;
    private Long uploaderId;
    private Map<Long, GroupRoleType> uploaderGroupRoles;
    private Long expectedSize;
    private String expectedMd5;
    private String mountTargetTagId;
}
