package com.oriole.wisepen.user.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum InfoPointChangeType {
    MARKET_PURCHASE(1, "集市购买"),
    MARKET_INCOME(2, "集市收入"),
    EXCHANGE_TO_TOKEN(3, "兑换成Token"),
    EXCHANGE_FROM_TOKEN(4, "Token兑换收入"),
    ADMIN_GRANT(5, "管理员发放"),
    ADMIN_YIELD(6, "管理员扣除"),
    MARKET_TRADE_REVOKE(7, "集市交易撤销"),
    CHECKIN_REWARD(8, "签到奖励");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

    public static InfoPointChangeType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElse(null);
    }
}
