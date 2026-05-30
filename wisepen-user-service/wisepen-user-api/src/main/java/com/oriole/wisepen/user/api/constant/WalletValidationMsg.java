package com.oriole.wisepen.user.api.constant;

public interface WalletValidationMsg {
    String INFO_POINT_CHANGE_TYPE_NOT_NULL = "交易类型不能为空";
    String INFO_POINT_CHANGE_AMOUNT_NOT_NULL = "变动数量不能为空";
    String INFO_POINT_CHANGE_AMOUNT_NOT_ZERO = "变动数量不能为0";

    String INFO_POINT_TRADE_RELATED_ID_NOT_NULL = "交易关联ID不能为空";
    String INFO_POINT_TRADE_USER_ID_NOT_NULL = "用户ID不能为空";
    String INFO_POINT_TRADE_PRICE_NOT_NULL = "交易价格不能为空";
    String INFO_POINT_INVALID_PRICE = "价格必须大于零";
    String INFO_POINT_SELF_TRANSACTION_NOT_ALLOWED = "不能与自己交易";
    String INFO_POINT_TRADE_REVERSE_REASON_NOT_NULL = "冲正原因不能为空";
}
