package com.oriole.wisepen.user.api.domain.dto.req;
import lombok.Data;

@Data
public class UserInfoUpdateRequest {
    private String nickname;
    private String avatar;
    private String realName;
}
