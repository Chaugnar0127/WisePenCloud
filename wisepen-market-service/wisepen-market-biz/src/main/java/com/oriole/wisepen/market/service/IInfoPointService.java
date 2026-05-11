package com.oriole.wisepen.market.service;

import com.oriole.wisepen.market.api.domain.dto.req.InfoPointChangeRequest;
import com.oriole.wisepen.market.api.enums.ExchangeDirection;

public interface IInfoPointService {
    void changeBalance(InfoPointChangeRequest infoPointChangeReq);
    void marketTransaction(Long buyerId, Long sellerId, Integer price, String relatedId);
    void exchangeCurrency(Long userId, Integer amount, ExchangeDirection direction);
}
