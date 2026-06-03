package com.oriole.wisepen.ai.asset.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum SkillSourceTypeEnum {
    BY_AGENT("BY_AGENT"),
    MANUAL("MANUAL");

    @JsonValue
    private final String code;

    @JsonCreator
    public static SkillSourceTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
