package com.oriole.wisepen.system.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户反馈类型枚举
 * @author Xiong.Heng
 */
@Getter
@AllArgsConstructor
public enum FeedbackType {
    BUG_REPORT(1, "BUG_REPORT"),
    SUGGESTION(2, "SUGGESTION"),
    CONSULTATION(3, "CONSULTATION"),
    COMPLAINT(4, "COMPLAINT"),
    // 无法归类到以上几类的反馈
    OTHER(99, "其他");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

}
