package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.enums.PreviewType;
import com.oriole.wisepen.resource.enums.ResourceMarketValidationMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ResourceUpdateSellRequest {
    @NotBlank(message = ResourceValidationMsg.RESOURCE_ID_NOT_BLANK)
    private String resourceId;

    @NotBlank(message = ResourceMarketValidationMessage.SELL_ID_NOT_BLANK)
    private String sellId;

    @Positive(message = ResourceMarketValidationMessage.PRICE_POSITIVE)
    private Integer price;

    private PreviewType previewType;
    private String tagId;
}
