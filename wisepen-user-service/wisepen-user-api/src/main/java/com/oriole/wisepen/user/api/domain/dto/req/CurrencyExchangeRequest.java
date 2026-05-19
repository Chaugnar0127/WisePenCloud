package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.constant.WalletValidationMsg;
import com.oriole.wisepen.user.api.domain.base.InfoPointBase;
import com.oriole.wisepen.user.api.enums.ExchangeDirection;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyExchangeRequest extends InfoPointBase {
    @NotNull(message = WalletValidationMsg.INFO_POINT_EXCHANGE_DIRECTION_NOT_NULL)
    private ExchangeDirection exchangeDirection;
}
