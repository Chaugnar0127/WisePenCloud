package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckRole;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.user.api.domain.dto.UserInfoDTO;
import com.oriole.wisepen.user.api.domain.dto.req.AdminUserListRequest;
import com.oriole.wisepen.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@CheckRole(IdentityType.ADMIN)
public class AdminUserController {

    private final UserService userService;

    @PostMapping("/list")
    @Log(title = "管理员查询用户列表", businessType = BusinessType.SELECT)
    public R<PageResult<UserInfoDTO>> list(@RequestBody AdminUserListRequest req) {
        PageResult<UserInfoDTO> page = userService.adminList(req.getPage(), req.getSize(), req.getKeyword(), req.getStatus(), req.getIdentityType());
        return R.ok(page);
    }

    @GetMapping("/{userId}")
    @Log(title = "管理员获取用户详情", businessType = BusinessType.SELECT)
    public R<UserInfoDTO> getUser(@PathVariable("userId") Long userId) {
        UserInfoDTO dto = userService.getUserInfoById(userId);
        return R.ok(dto);
    }

    @PutMapping("/update")
    @Log(title = "管理员更新用户信息", businessType = BusinessType.UPDATE)
    public R<Void> update(@RequestBody UserInfoDTO dto) {
        Long operator = SecurityContextHolder.getUserId();
        userService.adminUpdate(operator, dto);
        return R.ok();
    }

    @PutMapping("/reset-pwd/{targetUserId}")
    @Log(title = "管理员重置用户密码", businessType = BusinessType.UPDATE)
    public R<Void> resetPwd(@PathVariable("targetUserId") Long targetUserId) {
        userService.adminResetPassword(targetUserId);
        return R.ok();
    }
}
