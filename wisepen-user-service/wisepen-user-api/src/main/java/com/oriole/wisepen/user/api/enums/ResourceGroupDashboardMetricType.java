package com.oriole.wisepen.user.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResourceGroupDashboardMetricType {
    RESOURCE_ADDED("RESOURCE_ADDED"),
    RESOURCE_READ("RESOURCE_READ"),
    RESOURCE_LIKE("RESOURCE_LIKE"),
    RESOURCE_COMMENT("RESOURCE_COMMENT"),
    RESOURCE_AI_CALL("RESOURCE_AI_CALL");

    @EnumValue
    @JsonValue
    private final String value;
}
