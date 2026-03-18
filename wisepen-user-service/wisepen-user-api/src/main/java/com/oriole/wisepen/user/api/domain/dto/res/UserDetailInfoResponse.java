package com.oriole.wisepen.user.api.domain.dto.res;

import com.oriole.wisepen.user.api.domain.base.UserInfoBase;
import com.oriole.wisepen.user.api.domain.base.UserProfileBase;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class UserDetailInfoResponse implements Serializable {
    UserInfoBase userInfo;
    UserProfileBase userProfile;
    List<String> readonlyFields;
}