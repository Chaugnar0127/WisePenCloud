package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.enums.FileOrganizationLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupResConfigResponse {
    private String groupId;
    private FileOrganizationLogic fileOrgLogic;
}
