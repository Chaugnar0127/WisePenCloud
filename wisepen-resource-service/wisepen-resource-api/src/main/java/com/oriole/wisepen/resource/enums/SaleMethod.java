package com.oriole.wisepen.resource.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SaleMethod {
    @JsonEnumDefaultValue
    COPY_SUBSCRIPTION(3, "订阅副本");

    private final int code;
    private final String desc;
}
