package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.constant.WalletValidationMsg;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InfoPointTradeRevokeRequest {
    @NotNull(message = WalletValidationMsg.INFO_POINT_TRADE_RELATED_ID_NOT_NULL)
    private Long relatedId;

    private String reason;
}
