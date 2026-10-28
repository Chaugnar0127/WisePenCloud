package com.oriole.wisepen.web.app.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SingleHtmlPageStatus {
    UPLOADING(0, "UPLOADING"),
    UPLOADED(1, "UPLOADED"),
    REGISTERING_RES(3, "REGISTERING_RES"),
    READY(4, "READY"),
    REGISTERING_RES_TIMEOUT(-2, "REGISTERING_RES_TIMEOUT"),
    FAILED(-3, "FAILED");

    private final int code;

    @EnumValue
    @JsonValue
    private final String value;
}
