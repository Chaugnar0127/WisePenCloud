package com.oriole.wisepen.user.api.domain.base;

import com.oriole.wisepen.user.api.constant.UserValidationMsg;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InfoPointBase implements Serializable {
    @NotNull(message = UserValidationMsg.USER_ID_EMPTY)
    private Long userId;
    private Integer changeAmount;
}
