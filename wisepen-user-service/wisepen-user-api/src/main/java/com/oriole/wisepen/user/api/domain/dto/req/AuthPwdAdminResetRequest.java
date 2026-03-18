package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.constant.UserRegexPatterns;
import com.oriole.wisepen.user.api.constant.UserValidationMsg;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.io.Serializable;

/**
 * 重置密码执行请求体
 *
 * @author Oriole
 */
@Data
public class AuthPwdAdminResetRequest implements Serializable {

    @NotNull(message = UserValidationMsg.USER_ID_EMPTY)
    private Long userId; // 新密码

    @Pattern(regexp = UserRegexPatterns.PASSWORD_PATTERN, message = UserValidationMsg.PASSWORD_INVALID)
    private String newPassword; // 新密码
}