package com.oriole.wisepen.user.api.domain.base;

import com.oriole.wisepen.user.api.enums.Status;
import com.oriole.wisepen.user.api.enums.UserVerificationMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class UserInfoBase extends UserDisplayBase {
    private String username; // 用户名
    private String campusNo; // 学工号

    private String email;
    private String mobile;

    private UserVerificationMode verificationMode;
    private Status status;
}
