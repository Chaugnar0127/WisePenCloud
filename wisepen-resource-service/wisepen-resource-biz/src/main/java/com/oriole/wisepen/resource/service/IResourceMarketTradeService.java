package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.user.api.enums.InfoPointTradeReverseReason;

/**
 * 集市信息点交易：同步 Feign 扣款；交割失败时 Feign 冲正（无 confirm 阶段）。
 */
public interface IResourceMarketTradeService {

    /** 按订单号扣买家、入卖家，钱包状态 PAID */
    void chargeMarketOrder(Long buyerId, Long sellerId, Integer price, Long orderId);

    boolean tryReversePaidTrade(Long orderId, InfoPointTradeReverseReason reason, String detail);
}
