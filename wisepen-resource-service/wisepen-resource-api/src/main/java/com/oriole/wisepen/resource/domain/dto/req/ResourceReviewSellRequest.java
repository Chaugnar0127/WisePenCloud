package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.enums.ResourceMarketValidationMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceReviewSellRequest {
    @NotBlank(message = ResourceValidationMsg.RESOURCE_ID_NOT_BLANK)
    private String resourceId;

    @NotBlank(message = ResourceMarketValidationMessage.SELL_ID_NOT_BLANK)
    private String sellId;

    @NotNull(message = ResourceMarketValidationMessage.REVIEW_RESULT_NOT_NULL)
    private Boolean approved;

    private String comment;
}
