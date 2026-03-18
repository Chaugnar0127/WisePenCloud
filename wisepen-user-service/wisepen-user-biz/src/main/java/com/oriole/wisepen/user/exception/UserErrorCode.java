package com.oriole.wisepen.user.exception;

import com.oriole.wisepen.common.core.exception.IErrorCode;
import com.oriole.wisepen.user.api.constant.UserValidationMsg;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户模块错误码枚举
 * 范围：1000-1999
 *
 * @author Oriole
 */
@Getter
@AllArgsConstructor
public enum UserErrorCode implements IErrorCode {

    // 用户/认证模块 (1000-1999)
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_LOCKED(1003, "账号已禁用"),
    USER_UNIDENTIFIED(1004, "账号尚未验证学生身份/学生邮箱"),

    USERNAME_EXISTED(1004, "用户名已存在"),

    EMAIL_SEND_ERROR(1005, "邮件发送失败"),
    PASSWORD_RESET_FAILED(1006, "密码重设失败"),

    CAMPUS_NO_EXISTED(1010, "学号已存在"),

    // 用户验证（Email）模块 (1101~1109)
    USER_VERIFICATION_EMAIL_FORMAT_ERROR(1101, "请提供有效的 .edu 或 .edu.cn 教育邮箱"),
    USER_VERIFICATION_EMAIL_EXISTED(1102, "该邮箱已被其他账号绑定"),
    USER_VERIFICATION_EMAIL_TOKEN_NO_EXISTED(1103, "该验证链接已过期"),

    // 用户验证（FudanUIS）模块 (1111~1119)
    USER_VERIFICATION_FUDAN_UIS_ACCOUNT_EMPTY(1111, "FudanUIS账号或密码不能为空"),
    USER_VERIFICATION_FUDAN_UIS_REQUEST_ERROR(1112, "发起UIS认证请求失败"),
    USER_VERIFICATION_FUDAN_UIS_FAILED(1113, "UIS认证请求失败");

    private final Integer code;
    private final String msg;
}