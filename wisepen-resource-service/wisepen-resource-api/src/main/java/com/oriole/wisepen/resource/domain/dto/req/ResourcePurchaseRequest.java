package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.enums.ResourceMarketValidationMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourcePurchaseRequest {
    @NotBlank(message = ResourceValidationMsg.RESOURCE_ID_NOT_BLANK)
    private String resourceId;

    @NotBlank(message = ResourceMarketValidationMessage.SELL_ID_NOT_BLANK)
    private String sellId;
}
