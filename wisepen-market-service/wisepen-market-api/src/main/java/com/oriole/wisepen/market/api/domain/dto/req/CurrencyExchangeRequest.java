package com.oriole.wisepen.market.api.domain.dto.req;


import com.oriole.wisepen.market.api.domain.base.InfoPointBase;
import com.oriole.wisepen.market.api.enums.ExchangeDirection;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class CurrencyExchangeRequest extends InfoPointBase {
    @NotNull(message = "换汇方向不能为空")
    private ExchangeDirection exchangeDirection;
}
