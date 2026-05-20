package com.oriole.wisepen.resource.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeliveryStatus {
    PAID(1, "已支付"),
    COMPLETED(2, "已交割"),
    FAILED(3, "交割失败");

    private final int code;
    private final String desc;
}
