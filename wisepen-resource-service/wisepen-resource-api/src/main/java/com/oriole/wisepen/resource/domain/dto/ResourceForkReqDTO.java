package com.oriole.wisepen.resource.domain.dto;


import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.enums.OwnershipTier;
import com.oriole.wisepen.resource.enums.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ResourceForkReqDTO {

    @NotBlank(message = ResourceValidationMsg.RESOURCE_ID_NOT_BLANK)
    private String resourceId;
    @NotNull(message = ResourceValidationMsg.RESOURCE_TYPE_NOT_NULL)
    private ResourceType resourceType;
    @NotNull(message = ResourceValidationMsg.OWNER_ID_NOT_BLANK)
    private String newOwnerId;
    @NotNull(message = ResourceValidationMsg.OWNERSHIP_TIER_NOT_NULL)
    private OwnershipTier tier;

}
