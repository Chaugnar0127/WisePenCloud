package com.oriole.wisepen.market.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
