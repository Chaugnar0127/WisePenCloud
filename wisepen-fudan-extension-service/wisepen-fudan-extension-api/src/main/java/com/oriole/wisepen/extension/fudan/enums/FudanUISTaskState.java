package com.oriole.wisepen.extension.fudan.enums;

import lombok.Getter;

@Getter
public enum FudanUISTaskState {
    PENDING(0, "处理中"),
    WAITING_SCAN(2, "等待用户扫码"),
    SUCCESS(1, "成功"),
    FAILED_ERROR(-1, "其他异常"),
    FAILED_AUTH(-2, "账号密码错误或需进行其他验证"),
    FAILED_TIMEOUT(-3, "扫码超时");

    private final int code;
    private final String desc;

    FudanUISTaskState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}