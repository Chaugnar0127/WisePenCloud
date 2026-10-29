package com.oriole.wisepen.user.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TourCode {
    SYSTEM_FEATURES_TOUR("SYSTEM_FEATURES_TOUR"),
    COURSE_FEATURES_TOUR("COURSE_FEATURES_TOUR"),
    AI_CHAT_FEATURES_TOUR("AI_CHAT_FEATURES_TOUR"),
    NOTE_FEATURES_TOUR("NOTE_FEATURES_TOUR"),
    DRIVE_FEATURES_TOUR("DRIVE_FEATURES_TOUR");

    @EnumValue
    @JsonValue
    private final String value;
}
