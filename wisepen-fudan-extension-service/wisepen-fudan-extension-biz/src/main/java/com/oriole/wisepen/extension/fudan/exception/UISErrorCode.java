package com.oriole.wisepen.extension.fudan.exception;

import com.oriole.wisepen.common.core.exception.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UISErrorCode implements IErrorCode {

    TASK_NOT_FOUND(5001, "任务不存在或已过期"),
    AUTH_FAILED(5002, "账号密码错误或需进行其他验证"),
    SCAN_TIMEOUT(5003, "扫码超时"),
    SYSTEM_ERROR(5004, "UIS服务内部异常");

    private final Integer code;
    private final String msg;
}