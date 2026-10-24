package com.oriole.wisepen.system.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BatchTaskStatus {
    CREATED("CREATED"),
    PENDING("PENDING"),
    RUNNING("RUNNING"),
    PAUSING("PAUSING"),
    PAUSED("PAUSED"),
    SUCCEEDED("SUCCEEDED"),
    PARTIAL_SUCCESS("PARTIAL_SUCCESS"),
    FAILED("FAILED");

    @EnumValue
    @JsonValue
    private final String value;

    public boolean isTerminal() {
        return this == SUCCEEDED || this == PARTIAL_SUCCESS || this == FAILED;
    }
}
