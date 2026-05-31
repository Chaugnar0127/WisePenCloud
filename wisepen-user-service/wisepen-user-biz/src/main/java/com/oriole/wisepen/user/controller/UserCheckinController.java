package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.user.api.domain.dto.res.UserCheckinResponse;
import com.oriole.wisepen.user.api.domain.dto.res.UserCheckinStatusResponse;
import com.oriole.wisepen.user.service.IUserCheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/checkin")
@RequiredArgsConstructor
@CheckLogin
public class UserCheckinController {

    private final IUserCheckinService userCheckinService;

    @PostMapping
    public R<UserCheckinResponse> checkin() {
        return R.ok(userCheckinService.checkin(SecurityContextHolder.getUserId()));
    }

    @GetMapping("/today")
    public R<UserCheckinStatusResponse> getTodayStatus() {
        return R.ok(userCheckinService.getTodayStatus(SecurityContextHolder.getUserId()));
    }
}
