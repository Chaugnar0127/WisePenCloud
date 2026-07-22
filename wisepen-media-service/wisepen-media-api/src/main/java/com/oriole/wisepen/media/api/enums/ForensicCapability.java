package com.oriole.wisepen.media.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ForensicCapability {
    READY(1, "READY"),
    WEAK(2, "WEAK"),
    UNAVAILABLE(3, "UNAVAILABLE"),
    FAILED(4, "FAILED");

    private final int code;

    @EnumValue
    @JsonValue
    private final String value;
}
