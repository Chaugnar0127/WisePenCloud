package com.oriole.wisepen.market.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InfoPointChangeType {
    MARKET_PURCHASE(1,"集市购买"),
    MARKET_INCOME(2,"集市收入"),
    EXCHANGE_TO_TOKEN(3,"兑换成Token"),
    EXCHANGE_FROM_TOKEN(4,"Token兑换收入"),
    ADMIN_GRANT(5,"管理员发放"),
    FESTIVAL_GRANT(50,"活动奖励"),
    ADMIN_YIELD(6,"管理员扣除");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
