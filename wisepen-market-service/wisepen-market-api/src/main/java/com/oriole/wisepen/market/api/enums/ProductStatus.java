package com.oriole.wisepen.market.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品状态枚举
 */
@Getter
@AllArgsConstructor
public enum ProductStatus {

    OFF_SHELF(0, "已下架"),
    ON_SHELF(1, "已上架");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
