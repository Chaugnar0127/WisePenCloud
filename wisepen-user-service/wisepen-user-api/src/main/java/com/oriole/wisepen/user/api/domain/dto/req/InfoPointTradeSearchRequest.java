package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.enums.InfoPointChangeType;
import com.oriole.wisepen.user.api.enums.InfoPointTradeStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InfoPointTradeSearchRequest {
    @NotNull
    private Long userId;

    private InfoPointChangeType changeType;
    private InfoPointTradeStatus tradeStatus;
    private Integer changeAmount;

    @Min(1)
    private Integer page = 1;

    @Min(1)
    private Integer size = 20;
}
