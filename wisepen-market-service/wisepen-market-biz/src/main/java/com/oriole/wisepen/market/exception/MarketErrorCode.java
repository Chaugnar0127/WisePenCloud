package com.oriole.wisepen.market.exception;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.oriole.wisepen.common.core.exception.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MarketErrorCode implements IErrorCode {

    INFO_POINT_INSUFFICIENT(6001, "余额不足"),
    INFO_POINT_CHANGE_FAILED(6002, "余额更改失败"),
    SELF_TRANSACTION_NOT_ALLOWED(6003, "不允许自买自卖"),
    INVALID_PRICE(6004, "价格必须大于零"),
    EXCHANGE_AMOUNT_INVALID(6005, "换汇量不合法"),
    PRODUCT_NOT_FOUND(6006, "商品不存在"),
    PRODUCT_PERMISSION_DENIED(6007, "没有该商品的操作权限"),
    PRODUCT_STOCK_INSUFFICIENT(6008, "库存不足");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String msg;
}