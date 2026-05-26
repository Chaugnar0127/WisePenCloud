package com.oriole.wisepen.resource.service.impl;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.resource.exception.ResourceError;
import com.oriole.wisepen.resource.service.IResourceMarketTradeService;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointTradeReverseRequest;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointTradeSettleRequest;
import com.oriole.wisepen.user.api.enums.InfoPointTradeReverseReason;
import com.oriole.wisepen.user.api.feign.RemoteWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceMarketTradeServiceImpl implements IResourceMarketTradeService {

    private final RemoteWalletService remoteWalletService;

    @Override
    public void chargeMarketOrder(Long buyerId, Long sellerId, Integer price, Long orderId) {
        R<Void> response = remoteWalletService.settleInfoPointTrade(InfoPointTradeSettleRequest.builder()
                .buyerId(buyerId)
                .sellerId(sellerId)
                .price(price)
                .relatedId(orderId)
                .build());
        if (response == null || !Integer.valueOf(200).equals(response.getCode())) {
            throw new ServiceException(ResourceError.RESOURCE_MARKET_TRADE_SETTLE_FAILED);
        }
        log.info("market order charged orderId={} buyerId={} sellerId={} price={}",
                orderId, buyerId, sellerId, price);
    }

    @Override
    public boolean tryReversePaidTrade(Long orderId, InfoPointTradeReverseReason reason, String detail) {
        try {
            R<Void> response = remoteWalletService.reverseInfoPointTrade(InfoPointTradeReverseRequest.builder()
                    .relatedId(orderId)
                    .reason(reason)
                    .detail(detail)
                    .build());
            if (response == null || !Integer.valueOf(200).equals(response.getCode())) {
                return false;
            }
            log.info("market trade reversed orderId={} reason={}", orderId, reason);
            return true;
        } catch (Exception ex) {
            log.error("market trade reverse failed orderId={} reason={} detail={}", orderId, reason, detail, ex);
            return false;
        }
    }
}
