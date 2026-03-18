package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.domain.base.UserProfileBase;
import com.oriole.wisepen.user.api.enums.DegreeLevel;
import com.oriole.wisepen.user.api.enums.GenderType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserProfileUpdateRequest extends UserProfileBase {
}
