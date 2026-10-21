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
    READY(2, "READY"),
    FAILED(-1, "FAILED");

    private final int code;

    @EnumValue
    @JsonValue
    private final String value;
}
