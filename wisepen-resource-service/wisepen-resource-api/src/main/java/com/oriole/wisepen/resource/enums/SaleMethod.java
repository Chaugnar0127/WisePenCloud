package com.oriole.wisepen.resource.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SaleMethod {
    LICENSED_ACCESS(1, "内容授权"),
    COPY_BUYOUT(2, "买断副本"),
    COPY_SUBSCRIPTION(3, "订阅副本");

    private final int code;
    private final String desc;
}
