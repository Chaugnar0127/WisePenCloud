package com.oriole.wisepen.user.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InfoPointTradeStatus {
    PAID(1, "已扣款"),
    TRADE_SUCCESS(2, "交易成功"),
    ADMIN_REVOKED(3, "管理员撤销交易");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
