package com.oriole.wisepen.resource.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TagIndexDisplay {
    HIDDEN(0, "不在首页展示"),
    SHOWN(1, "在首页展示");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
