package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.user.api.domain.dto.req.WalletRedeemVoucherRequest;
import com.oriole.wisepen.user.api.domain.dto.req.WalletTransferTokenRequest;
import com.oriole.wisepen.user.api.domain.dto.res.WalletDetailResponse;
import com.oriole.wisepen.user.api.domain.dto.res.WalletTransactionRecordResponse;
import com.oriole.wisepen.user.api.enums.WalletBusinessType;
import com.oriole.wisepen.user.api.enums.WalletPayerType;
import com.oriole.wisepen.user.api.enums.WalletTransactionType;
import com.oriole.wisepen.user.service.IWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户钱包", description = "用户钱包信息点、兑换码、转账与流水查询")
@RestController
@RequestMapping("/user/wallet")
@RequiredArgsConstructor
@Validated
@CheckLogin
public class WalletController {

    private final IWalletService walletService;

    @Operation(summary = "获取用户钱包信息")
    @GetMapping("/getUserWalletInfo")
    public R<WalletDetailResponse> getUserWalletInfo() {
        Long userId = SecurityContextHolder.getUserId();
        return R.ok(walletService.getUserWalletInfo(userId));
    }

    @Operation(summary = "兑换信息点券")
    @PostMapping("/redeemVoucher")
    public R<Void> redeemVoucher(@RequestBody WalletRedeemVoucherRequest req) {
        Long userId = SecurityContextHolder.getUserId();
        walletService.redeemVoucher(userId, req.getVoucherCode());
        return R.ok();
    }

    @Operation(summary = "转移用户与小组信息点")
    @PostMapping("/transferTokenBetweenGroupAndUser")
    public R<Void> transferTokenBetweenGroupAndUser(@RequestBody @Valid WalletTransferTokenRequest req) {
        Long userId = SecurityContextHolder.getUserId();
        SecurityContextHolder.assertGroupRole(req.getGroupId(), GroupRoleType.OWNER);
        walletService.transferTokenBetweenGroupAndUser(userId, req);
        return R.ok();
    }

    @Operation(summary = "分页查询钱包流水")
    @GetMapping("/listTransactions")
    public R<PageR<WalletTransactionRecordResponse>> listTransactions(
            @RequestParam(value = "groupId", required = false) Long groupId,
            @RequestParam(value = "walletTransactionType", required = false) WalletTransactionType walletTransactionType,
            @RequestParam(value = "walletBusinessType", required = false) WalletBusinessType walletBusinessType,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) Integer size
    ) {
        WalletPayerType payerType;
        Long payerId;
        if (groupId == null) {
            payerType = WalletPayerType.USER;
            payerId = SecurityContextHolder.getUserId();
        } else {
            payerType = WalletPayerType.GROUP;
            payerId = groupId;
            SecurityContextHolder.assertGroupRole(groupId, GroupRoleType.OWNER);
        }
        return R.ok(walletService.listTransactions(payerType, payerId, walletTransactionType, walletBusinessType, page, size));
    }
}
