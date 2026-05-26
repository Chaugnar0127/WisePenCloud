package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckRole;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointChangeRequest;
import com.oriole.wisepen.user.api.enums.InfoPointChangeType;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.user.exception.UserError;
import com.oriole.wisepen.user.service.IWalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/wallet/infopoint")
@RequiredArgsConstructor
@Validated
@CheckRole(IdentityType.ADMIN)
public class AdminWalletInfoPointController {

    private final IWalletService walletService;

    @GetMapping("/balance")
    @Log(title = "管理员查询用户信息点余额", businessType = BusinessType.SELECT)
    public R<Integer> getBalance(@RequestParam("userId") @NotNull Long userId) {
        return R.ok(walletService.getInfoPointBalance(userId));
    }

    @PostMapping("/changeBalance")
    @Log(title = "管理员调整用户信息点", businessType = BusinessType.UPDATE)
    public R<Void> changeBalance(@RequestBody @Valid InfoPointChangeRequest req) {
        if (req.getChangeType() != InfoPointChangeType.ADMIN_GRANT
                && req.getChangeType() != InfoPointChangeType.ADMIN_YIELD) {
            throw new ServiceException(UserError.WALLET_INFO_POINT_CHANGE_TYPE_INVALID);
        }
        req.setOperatorId(SecurityContextHolder.getUserId());
        walletService.changeInfoPointBalance(req);
        return R.ok();
    }
}
