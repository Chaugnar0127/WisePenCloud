package com.oriole.wisepen.user.api.constant;

public interface WalletValidationMsg {
    String TOKEN_COUNT_NOT_NULL = "划转数量不能为空";
    String TOKEN_COUNT_MIN = "划转数量必须大于等于1";
    String VOUCHER_CODE_NOT_BLANK = "兑换码不能为空";
    String INFO_POINT_CHANGE_TYPE_NOT_NULL = "交易类型不能为空";
    String INFO_POINT_EXCHANGE_DIRECTION_NOT_NULL = "换汇方向不能为空";
}
