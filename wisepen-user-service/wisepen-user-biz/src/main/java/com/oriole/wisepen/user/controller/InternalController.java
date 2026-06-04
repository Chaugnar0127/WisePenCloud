package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.user.api.domain.base.GroupDisplayBase;
import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import com.oriole.wisepen.user.api.domain.dto.req.WalletSettleCoinTradeRequest;
import com.oriole.wisepen.user.api.feign.RemoteUserService;
import com.oriole.wisepen.user.api.feign.RemoteWalletService;
import com.oriole.wisepen.user.service.IGroupService;
import com.oriole.wisepen.user.service.IUserService;
import com.oriole.wisepen.user.service.IWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Tag(name = "内部用户服务", description = "供其他微服务调用的用户、小组与钱包接口")
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController implements RemoteUserService, RemoteWalletService {

    private final IUserService userService;
    private final IGroupService groupService;
    private final IWalletService walletService;

    @Override
    @Operation(summary = "内部获取用户展示信息")
    @GetMapping("/user/getUserDisplayInfo")
    public R<Map<Long, UserDisplayBase>> getUserDisplayInfo(List<Long> userIds) {
        return R.ok(userService.getUserDisplayInfoByIds(new HashSet<>(userIds)));
    }

    @Override
    @Operation(summary = "内部获取小组展示信息")
    @GetMapping("/group/getGroupDisplayInfo")
    public R<Map<Long, GroupDisplayBase>> getGroupDisplayInfo(List<Long> groupIds) {
        return R.ok(groupService.getGroupDisplayInfoByIds(new HashSet<>(groupIds)));
    }


    @Override
    @Operation(summary = "内部结算信息点交易")
    @PostMapping("/user/wallet/settleTrade")
    public R<Void> settleCoinTrade(@RequestBody @Valid WalletSettleCoinTradeRequest req) {
        walletService.settleCoinTrade(req);
        return R.ok();
    }
}
