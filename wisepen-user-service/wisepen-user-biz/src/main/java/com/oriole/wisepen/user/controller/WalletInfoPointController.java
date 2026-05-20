package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.user.api.domain.dto.req.CurrencyExchangeRequest;
import com.oriole.wisepen.user.api.domain.dto.res.InfoPointTransactionRecordResponse;
import com.oriole.wisepen.user.api.enums.InfoPointChangeType;
import com.oriole.wisepen.user.service.IWalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/wallet/infopoint")
@RequiredArgsConstructor
@Validated
@CheckLogin
public class WalletInfoPointController {

    private final IWalletService walletService;

    @GetMapping("/balance")
    public R<Integer> getBalance() {
        return R.ok(walletService.getInfoPointBalance(SecurityContextHolder.getUserId()));
    }

    @PostMapping("/exchange")
    public R<Void> exchangeCurrency(@RequestBody @Valid CurrencyExchangeRequest req) {
        req.setUserId(SecurityContextHolder.getUserId());
        walletService.exchangeCurrency(req);
        return R.ok();
    }

    @GetMapping("/records")
    public R<PageR<InfoPointTransactionRecordResponse>> getRecordList(
            @RequestParam(value = "changeType", required = false) InfoPointChangeType changeType,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) Integer size
    ) {
        return R.ok(walletService.listInfoPointTransactions(SecurityContextHolder.getUserId(), changeType, page, size));
    }
}
