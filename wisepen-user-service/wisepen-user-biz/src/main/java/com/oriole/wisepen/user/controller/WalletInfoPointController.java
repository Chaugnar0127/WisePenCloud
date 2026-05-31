package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.user.service.IInfoPointWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/wallet/infopoint")
@RequiredArgsConstructor
@Validated
@CheckLogin
public class WalletInfoPointController {

    private final IInfoPointWalletService walletService;

    @GetMapping("/getBalance")
    public R<Integer> getInfoPointBalance() {
        Long userId = SecurityContextHolder.getUserId();
        return R.ok(walletService.getInfoPointBalance(userId));
    }
}
