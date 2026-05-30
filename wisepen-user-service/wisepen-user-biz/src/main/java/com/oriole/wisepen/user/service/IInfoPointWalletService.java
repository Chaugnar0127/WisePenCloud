package com.oriole.wisepen.user.service;

import com.oriole.wisepen.user.api.domain.dto.req.InfoPointChangeRequest;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointTradeReverseRequest;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointTradeSettleRequest;

public interface IInfoPointWalletService {

    // 改变个人信息点余额（集市交易/管理员调账共用）
    void changeInfoPointBalance(InfoPointChangeRequest req);

    // 查询信息点余额
    Integer getInfoPointBalance(Long userId);

    // 集市交易：买家扣款、卖家入账（PAID，幂等）
    void settleInfoPointTrade(InfoPointTradeSettleRequest req);

    // 集市交易：冲正退款（PAID -> ADMIN_REVOKED）
    void reverseInfoPointTrade(InfoPointTradeReverseRequest req);
}
