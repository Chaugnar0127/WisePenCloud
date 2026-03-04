package com.oriole.wisepen.user.service;

import com.oriole.wisepen.user.api.domain.dto.*;
import com.oriole.wisepen.user.domain.entity.User;

public interface UserService {
    User getUserCoreInfoByAccount(String account);
    UserInfoDTO getUserInfoById(Long userId);

    void register(RegisterRequest registerRequest);
    void sendResetMail(ResetRequest resetRequest);
    void resetPassword(ResetExecuteRequest resetExecuteRequest);
}