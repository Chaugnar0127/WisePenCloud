package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.constant.WalletValidationMsg;
import com.oriole.wisepen.user.api.enums.InfoPointChangeType;
import com.oriole.wisepen.user.api.enums.InfoPointTradeStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InfoPointTradeSearchRequest {
    @NotNull(message = WalletValidationMsg.INFO_POINT_TRADE_USER_ID_NOT_NULL)
    private Long userId;

    private InfoPointChangeType changeType;
    private InfoPointTradeStatus tradeStatus;
    private Integer changeAmount;

    @Min(value = 1, message = WalletValidationMsg.PAGE_MIN)
    private Integer page = 1;

    @Min(value = 1, message = WalletValidationMsg.SIZE_MIN)
    private Integer size = 20;
}
