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
    // 蚆蛒
    BUG(1, "问题报错"),

    // 用户希望增加的新功能，或者现有的流程优化
    SUGGESTION(2, "功能建议"),

    // 用户不会操作某个功能，提问求助
    CONSULTATION(3, "使用咨询"),

    // 对产品体验、运营活动或客服态度的不满
    COMPLAINT(4, "服务投诉"),

    // 无法归类到以上几类的反馈
    OTHER(99, "其他");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

}
