package com.oriole.wisepen.resource.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标签可见性模式枚举
 */
@Getter
@AllArgsConstructor
public enum AclGrantMode {

    ALL(0),        // 全员下发配置的 Mask
    ONLY_ADMIN(1), // 仅管理员下发 Mask（普通成员下发 0）
    WHITELIST(2),  // 仅白名单内用户下发配置的 Mask
    BLACKLIST(3);  // 仅黑名单外用户下发配置的 Mask

    @EnumValue
    @JsonValue
    private final int code;
}