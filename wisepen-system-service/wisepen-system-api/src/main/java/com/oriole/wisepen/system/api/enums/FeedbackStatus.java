package com.oriole.wisepen.system.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户反馈处理状态枚举
 * @author Xiong.Heng
 */
@Getter
@AllArgsConstructor
public enum FeedbackStatus {
    // 用户刚提交，后台客服/管理员尚未查看
    PENDING(0, "待处理"),

    // 客服已接手，或者已转交技术团队排查中
    PROCESSING(1, "处理中"),

    // 问题已修复，或已给用户满意答复
    RESOLVED(2, "已解决"),

    // 垃圾信息、无效反馈或重复反馈
    IGNORED(3, "已忽略"),

    // 长期无法联系到用户，或流程自然终结（可选）
    CLOSED(4, "已关闭");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

}
