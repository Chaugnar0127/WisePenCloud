package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.domain.base.UserInfoBase;
import com.oriole.wisepen.user.api.domain.base.UserProfileBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserInfoAdminUpdateRequest extends UserInfoBase {
    private Long userId;
}
