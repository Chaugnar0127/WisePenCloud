 package com.oriole.wisepen.user.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GroupDashboardSubjectType {
    RESOURCE("RESOURCE"),
    AI_CHAT("AI_CHAT");

    @EnumValue
    @JsonValue
    private final String value;
}
