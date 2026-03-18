package com.oriole.wisepen.user.service;

import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import com.oriole.wisepen.user.api.domain.base.UserProfileBase;
import com.oriole.wisepen.user.api.domain.dto.req.*;
import com.oriole.wisepen.user.api.domain.dto.res.UserDetailInfoResponse;
import com.oriole.wisepen.user.api.enums.Status;
import com.oriole.wisepen.user.domain.entity.UserEntity;
import com.oriole.wisepen.user.domain.entity.UserProfileEntity;

import java.util.Map;
import java.util.Set;

public interface UserService {
    UserEntity getUserCoreInfoByAccount(String account);
    UserDetailInfoResponse getUserInfoById(Long userId);

    UserDisplayBase getUserDisplayInfoById(Long userId);
    Map<Long, UserDisplayBase> getUserDisplayInfoByIds(Set<Long> userIds);

    void register(AuthRegisterRequest req);

    void sendResetMail(AuthPwdResetVerifyRequest req);
    void resetPassword(AuthPwdResetRequest req);
    void updateUserInfo(Long userId, UserInfoUpdateRequest req);
    void updateProfile(Long userId, UserProfileUpdateRequest req);

    // 仅限管理员使用
    void resetPasswordAdmin(AuthPwdAdminResetRequest req);
    void updateUserInfoAdmin(UserInfoAdminUpdateRequest req);
    void updateProfileAdmin(UserProfileAdminUpdateRequest req);
    PageResult<UserEntity> getUserListAdmin(int page, int size, String keyword, Status status, IdentityType identityType);
    UserProfileEntity getUserDetailInfoAdmin(Long userId);
}