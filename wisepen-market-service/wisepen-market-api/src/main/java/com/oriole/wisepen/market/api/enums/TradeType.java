package com.oriole.wisepen.market.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易内容类型枚举
 */
@Getter
@AllArgsConstructor
public enum TradeType {

    USE_RIGHT(1, "使用权"),
    OWNERSHIP(2, "所有权");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
