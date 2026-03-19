package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.enums.FileOrganizationLogic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupResConfigUpdateRequest {

    @NotBlank(message = ResourceValidationMsg.GROUP_ID_NOT_BLANK)
    private String groupId;

    @NotNull(message = ResourceValidationMsg.FILE_ORG_LOGIC_NOT_NULL)
    private FileOrganizationLogic fileOrgLogic;
}
