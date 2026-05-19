package com.oriole.wisepen.user.exception;

import com.oriole.wisepen.common.core.domain.IResult;
import com.oriole.wisepen.common.core.domain.ResultKey;
import com.oriole.wisepen.common.core.domain.enums.BusinessDomain;
import com.oriole.wisepen.common.core.exception.ErrorReason;
import com.oriole.wisepen.user.api.constant.UserSubject;
import com.oriole.wisepen.user.api.enums.UserErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户存储微服务(3)专属业务错误
 */
@Getter
@AllArgsConstructor
public enum UserError implements IResult {

    // 认证相关异常
    AUTH_USERNAME_OR_PASSWORD_INVALID(3111, new ResultKey(BusinessDomain.USER, UserSubject.AUTH, ErrorReason.INVALID), UserErrorMessage.AUTH_USERNAME_OR_PASSWORD_INVALID.getMsg()),
    AUTH_USER_LOCKED(3121, new ResultKey(BusinessDomain.USER, UserSubject.AUTH, ErrorReason.LOCKED), UserErrorMessage.AUTH_USER_LOCKED.getMsg()),

    // 用户相关异常
    USERNAME_ALREADY_EXISTS(3211, new ResultKey(BusinessDomain.USER, UserSubject.USER, ErrorReason.ALREADY_EXISTS), UserErrorMessage.USERNAME_ALREADY_EXISTS.getMsg()),
    CAMPUS_NO_ALREADY_EXISTS(3212, new ResultKey(BusinessDomain.USER, UserSubject.USER, ErrorReason.ALREADY_EXISTS), UserErrorMessage.CAMPUS_NO_ALREADY_EXISTS.getMsg()),
    USER_PASSWORD_RESET_EXPIRED(3221,  new ResultKey(BusinessDomain.USER, UserSubject.USER, ErrorReason.EXPIRED), UserErrorMessage.USER_PASSWORD_RESET_EXPIRED.getMsg()),
    USER_PASSWORD_RESET_EMAIL_SEND_FAILED(3231, new ResultKey(BusinessDomain.USER, UserSubject.USER, ErrorReason.FAILED), UserErrorMessage.USER_PASSWORD_RESET_EMAIL_SEND_FAILED.getMsg()),

    // 用户验证相关异常
    CANNOT_OPERATE_BEFORE_AUTH_VERIFICATION(3311, new ResultKey(BusinessDomain.USER, UserSubject.AUTH_VERIFICATION, ErrorReason.STATE_INVALID), UserErrorMessage.CANNOT_OPERATE_BEFORE_AUTH_VERIFICATION.getMsg()),
    VERIFICATION_EMAIL_INVALID(3312, new ResultKey(BusinessDomain.USER, UserSubject.AUTH_VERIFICATION, ErrorReason.INVALID), UserErrorMessage.VERIFICATION_EMAIL_INVALID.getMsg()),
    VERIFICATION_EMAIL_ALREADY_EXISTS(3321, new ResultKey(BusinessDomain.USER, UserSubject.AUTH_VERIFICATION, ErrorReason.ALREADY_EXISTS), UserErrorMessage.VERIFICATION_EMAIL_ALREADY_EXISTS.getMsg()),
    VERIFICATION_CAMPUS_NO_ALREADY_EXISTS(3322, new ResultKey(BusinessDomain.USER, UserSubject.AUTH_VERIFICATION, ErrorReason.ALREADY_EXISTS), UserErrorMessage.VERIFICATION_CAMPUS_NO_ALREADY_EXISTS.getMsg()),
    VERIFICATION_EMAIL_TOKEN_EXPIRED(3331, new ResultKey(BusinessDomain.USER, UserSubject.AUTH_VERIFICATION, ErrorReason.EXPIRED), UserErrorMessage.VERIFICATION_EMAIL_TOKEN_EXPIRED.getMsg()),
    VERIFICATION_EMAIL_SEND_FAILED(3341, new ResultKey(BusinessDomain.USER, UserSubject.AUTH_VERIFICATION, ErrorReason.FAILED), UserErrorMessage.VERIFICATION_EMAIL_SEND_FAILED.getMsg()),
    VERIFICATION_FUDAN_UIS_REQUEST_FAILED(3342, new ResultKey(BusinessDomain.USER, UserSubject.AUTH_VERIFICATION, ErrorReason.FAILED), UserErrorMessage.VERIFICATION_FUDAN_UIS_REQUEST_FAILED.getMsg()),
    VERIFICATION_FUDAN_UIS_FAILED(3343, new ResultKey(BusinessDomain.USER, UserSubject.AUTH_VERIFICATION, ErrorReason.FAILED), UserErrorMessage.VERIFICATION_FUDAN_UIS_FAILED.getMsg()),

    // 小组相关异常
    GROUP_NOT_EXIST(3411, new ResultKey(BusinessDomain.USER, UserSubject.GROUP, ErrorReason.NOT_FOUND), UserErrorMessage.GROUP_NOT_EXIST.getMsg()),

    // 小组成员相关异常
    GROUP_MEMBER_NOT_FOUND(3511, new ResultKey(BusinessDomain.USER, UserSubject.GROUP_MEMBER, ErrorReason.NOT_FOUND), UserErrorMessage.GROUP_MEMBER_NOT_FOUND.getMsg()),
    GROUP_MEMBER_ALREADY_EXISTS(3521, new ResultKey(BusinessDomain.USER, UserSubject.GROUP_MEMBER, ErrorReason.ALREADY_EXISTS), UserErrorMessage.GROUP_MEMBER_ALREADY_EXISTS.getMsg()),
    CANNOT_QUIT_GROUP_AS_OWNER(3531, new ResultKey(BusinessDomain.USER, UserSubject.GROUP_MEMBER, ErrorReason.NOT_ALLOWED), UserErrorMessage.CANNOT_QUIT_GROUP_AS_OWNER.getMsg()),

    // 钱包相关异常
    CANNOT_CONFIGURE_GROUP_WALLET_QUOTA(3611, new ResultKey(BusinessDomain.USER, UserSubject.WALLET, ErrorReason.NOT_ALLOWED), UserErrorMessage.CANNOT_CONFIGURE_GROUP_WALLET_QUOTA.getMsg()),
    // TOKEN钱包相关异常
    WALLET_TOKEN_LIMIT_BELOW_USED(3711, new ResultKey(BusinessDomain.USER, UserSubject.WALLET_TOKEN, ErrorReason.BELOW_LOWER_BOUND), UserErrorMessage.WALLET_TOKEN_LIMIT_BELOW_USED.getMsg()),
    // 信息点钱包相关异常
    WALLET_INFO_POINT_INSUFFICIENT(3911, new ResultKey(BusinessDomain.USER, UserSubject.WALLET_INFO_POINT, ErrorReason.BELOW_LOWER_BOUND), UserErrorMessage.WALLET_INFO_POINT_INSUFFICIENT.getMsg()),
    WALLET_INFO_POINT_CHANGE_FAILED(3912, new ResultKey(BusinessDomain.USER, UserSubject.WALLET_INFO_POINT, ErrorReason.FAILED), UserErrorMessage.WALLET_INFO_POINT_CHANGE_FAILED.getMsg()),
    WALLET_INFO_POINT_SELF_TRANSACTION_NOT_ALLOWED(3913, new ResultKey(BusinessDomain.USER, UserSubject.WALLET_INFO_POINT, ErrorReason.NOT_ALLOWED), UserErrorMessage.WALLET_INFO_POINT_SELF_TRANSACTION_NOT_ALLOWED.getMsg()),
    WALLET_INFO_POINT_INVALID_PRICE(3914, new ResultKey(BusinessDomain.USER, UserSubject.WALLET_INFO_POINT, ErrorReason.INVALID), UserErrorMessage.WALLET_INFO_POINT_INVALID_PRICE.getMsg()),
    WALLET_INFO_POINT_EXCHANGE_AMOUNT_INVALID(3915, new ResultKey(BusinessDomain.USER, UserSubject.WALLET_INFO_POINT, ErrorReason.INVALID), UserErrorMessage.WALLET_INFO_POINT_EXCHANGE_AMOUNT_INVALID.getMsg()),
    // TOKEN点卡相关异常
    WALLET_VOUCHER_NOT_FOUND(3811, new ResultKey(BusinessDomain.USER, UserSubject.VOUCHER, ErrorReason.NOT_FOUND), UserErrorMessage.WALLET_VOUCHER_NOT_FOUND.getMsg()),
    WALLET_VOUCHER_INVALID(3821, new ResultKey(BusinessDomain.USER, UserSubject.VOUCHER, ErrorReason.INVALID), UserErrorMessage.WALLET_VOUCHER_INVALID.getMsg()),
    WALLET_VOUCHER_EXPIRED(3831, new ResultKey(BusinessDomain.USER, UserSubject.VOUCHER, ErrorReason.EXPIRED), UserErrorMessage.WALLET_VOUCHER_EXPIRED.getMsg());

    private final Integer code;
    private final ResultKey key;
    private final String msg;
}
