package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.constant.WalletValidationMsg;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfoPointTradeSettleRequest {

    @NotNull(message = WalletValidationMsg.INFO_POINT_TRADE_USER_ID_NOT_NULL)
    private Long buyerId;

    @NotNull(message = WalletValidationMsg.INFO_POINT_TRADE_USER_ID_NOT_NULL)
    private Long sellerId;

    @NotNull(message = WalletValidationMsg.INFO_POINT_TRADE_PRICE_NOT_NULL)
    @Positive(message = WalletValidationMsg.INFO_POINT_INVALID_PRICE)
    private Integer price;

    @NotNull(message = WalletValidationMsg.INFO_POINT_TRADE_RELATED_ID_NOT_NULL)
    private Long relatedId;
}
