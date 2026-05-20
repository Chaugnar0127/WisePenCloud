package com.oriole.wisepen.resource.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SaleMethod {
    USE_RIGHT(1, "使用权"),
    OWNERSHIP(2, "所有权"),
    SUBSCRIPTION(3, "订阅");

    private final int code;
    private final String desc;
}
