package com.oriole.wisepen.user.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InfoPointTradeReverseReason {
    ADMIN,
    DELIVERY_FAILED,
    PURCHASE_ABORT
}
