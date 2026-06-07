package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.enums.MarketListingAuditStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MarketAuditListingRequest {
    @NotBlank(message = ResourceValidationMsg.MARKET_LISTING_ID_NOT_BLANK)
    private String listingId;

    @NotBlank(message = ResourceValidationMsg.GROUP_ID_NOT_BLANK)
    private String marketGroupId;

    @NotNull(message = ResourceValidationMsg.MARKET_AUDIT_STATUS_NOT_NULL)
    private MarketListingAuditStatus auditStatus;

    private String auditMessage;
}
