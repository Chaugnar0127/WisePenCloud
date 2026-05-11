package com.oriole.wisepen.market.api.domain.dto.req;


import com.oriole.wisepen.market.api.enums.ExchangeDirection;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class CurrencyExchangeRequest {

    private Long userId;
    private Integer amount;

    @NotNull(message = "换汇方向不能为空")
    private ExchangeDirection exchangeDirection;
}
