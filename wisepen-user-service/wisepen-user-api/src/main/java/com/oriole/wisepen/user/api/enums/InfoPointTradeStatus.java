package com.oriole.wisepen.user.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum InfoPointTradeStatus {
    PAID(1, "付款成功"),
    ADMIN_REVOKED(2, "管理员撤回");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

    public static InfoPointTradeStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(null);
    }
}
