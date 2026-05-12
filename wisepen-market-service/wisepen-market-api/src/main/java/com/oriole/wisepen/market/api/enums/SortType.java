package com.oriole.wisepen.market.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortType {
    TIME_ASC(0,"时间升序"),
    TIME_DESC(1,"时间降序"),
    PRICE_ASC(2,"价格升序"),
    PRICE_DESC(3,"价格降序"),
    SALESCOUNT_ASC(4,"销量降序"),
    SALESCOUNT_DESC(5,"销量降序"),
    HOTSCORE_ASC(6,"热门降序"),
    HOTSCORE_DESC(67,"热门降序");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
