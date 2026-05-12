package com.oriole.wisepen.market.service;

import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.market.api.domain.dto.req.CurrencyExchangeRequest;
import com.oriole.wisepen.market.api.domain.dto.req.InfoPointChangeRequest;
import com.oriole.wisepen.market.api.domain.dto.res.InfoPointTransactionRecordResponse;
import com.oriole.wisepen.market.api.enums.InfoPointChangeType;

public interface IInfoPointService {
    void changeBalance(InfoPointChangeRequest req);
    void handleTransaction(Long buyerId, Long sellerId, Integer price, String relatedId);
    void exchangeCurrency(CurrencyExchangeRequest req);
    Integer getBalance(Long userId);
    PageResult<InfoPointTransactionRecordResponse> getRecordlist(Long userId, InfoPointChangeType changeType, Integer page, Integer size);
}
