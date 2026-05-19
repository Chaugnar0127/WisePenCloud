package com.oriole.wisepen.user.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExchangeDirection {

    INFOPOINT_TO_TOKEN(0, "货币换token"),
    TOKEN_TO_INFOPOINT(1, "token换货币");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
