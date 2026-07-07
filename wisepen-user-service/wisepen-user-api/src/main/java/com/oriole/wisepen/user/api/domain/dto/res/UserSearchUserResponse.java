package com.oriole.wisepen.user.api.domain.dto.res;

import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserSearchUserResponse extends UserDisplayBase {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
}
