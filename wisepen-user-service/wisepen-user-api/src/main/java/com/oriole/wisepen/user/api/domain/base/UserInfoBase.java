package com.oriole.wisepen.user.api.domain.base;

import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.user.api.enums.Status;
import com.oriole.wisepen.user.api.enums.UserVerificationMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class UserInfoBase extends UserDisplayBase {
    private UserVerificationMode verificationMode;
    private Status status;
}
