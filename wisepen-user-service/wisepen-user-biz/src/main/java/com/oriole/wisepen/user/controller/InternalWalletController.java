package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointTradeReverseRequest;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointTradeSettleRequest;
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
    public R<Void> settleInfoPointTrade(@RequestBody @Valid InfoPointTradeSettleRequest req) {
        walletService.settleInfoPointTrade(req);
        return R.ok();
    }

    @Override
    public R<Void> reverseInfoPointTrade(@RequestBody @Valid InfoPointTradeReverseRequest req) {
        // 内部 Feign 无登录态，不设置 operatorId；对外 Controller 需注入 req.operatorId
        walletService.reverseInfoPointTrade(req);
        return R.ok();
    }

    @Override
    public R<Integer> getInfoPointBalance(Long userId) {
        return R.ok(walletService.getInfoPointBalance(userId));
    }
}
