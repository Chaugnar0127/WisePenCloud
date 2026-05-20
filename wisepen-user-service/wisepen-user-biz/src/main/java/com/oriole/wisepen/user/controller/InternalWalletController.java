package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.user.api.domain.dto.req.CurrencyExchangeRequest;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointChangeRequest;
import com.oriole.wisepen.user.api.domain.dto.res.InfoPointTransactionRecordResponse;
import com.oriole.wisepen.user.api.enums.InfoPointChangeType;
import com.oriole.wisepen.user.api.feign.RemoteWalletService;
import com.oriole.wisepen.user.service.IWalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InternalWalletController implements RemoteWalletService {

    private final IWalletService walletService;

    @Override
    public R<Void> changeInfoPointBalance(@RequestBody @Valid InfoPointChangeRequest req) {
        walletService.changeInfoPointBalance(req);
        return R.ok();
    }

    @Override
    public R<Void> settleInfoPointTrade(Long buyerId, Long sellerId, Integer price, Long relatedId) {
        walletService.settleInfoPointTrade(buyerId, sellerId, price, relatedId);
        return R.ok();
    }

    @Override
    public R<Void> exchangeCurrency(@RequestBody @Valid CurrencyExchangeRequest req) {
        walletService.exchangeCurrency(req);
        return R.ok();
    }

    @Override
    public R<Integer> getInfoPointBalance(Long userId) {
        return R.ok(walletService.getInfoPointBalance(userId));
    }

    @Override
    public R<PageR<InfoPointTransactionRecordResponse>> getInfoPointRecords(
            Long userId,
            InfoPointChangeType changeType,
            Integer page,
            Integer size
    ) {
        return R.ok(walletService.listInfoPointTransactions(userId, changeType, page, size));
    }
}
