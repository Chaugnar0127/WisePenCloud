package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.enums.PreviewType;
import com.oriole.wisepen.resource.enums.ResourceMarketValidationMessage;
import com.oriole.wisepen.resource.enums.SaleMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ResourcePublishSellRequest {
    @NotBlank(message = ResourceValidationMsg.RESOURCE_ID_NOT_BLANK)
    private String resourceId;

    @NotBlank(message = ResourceMarketValidationMessage.GROUP_ID_NOT_BLANK)
    private String groupId;

    @NotBlank(message = ResourceMarketValidationMessage.TAG_ID_NOT_BLANK)
    private String tagId;

    @NotNull(message = ResourceMarketValidationMessage.PRICE_NOT_NULL)
    @Positive(message = ResourceMarketValidationMessage.PRICE_POSITIVE)
    private Integer price;

    @NotNull(message = ResourceMarketValidationMessage.SALE_METHOD_NOT_NULL)
    private SaleMethod saleMethod;

    @NotNull(message = ResourceMarketValidationMessage.PREVIEW_TYPE_NOT_NULL)
    private PreviewType previewType;
}
