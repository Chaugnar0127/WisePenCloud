package com.oriole.wisepen.user.api.domain.base;

import com.oriole.wisepen.user.api.constant.UserValidationMsg;
import com.oriole.wisepen.user.api.constant.WalletValidationMsg;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
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

    @NotNull(message = WalletValidationMsg.INFO_POINT_CHANGE_AMOUNT_NOT_NULL)
    private Integer changeAmount;

    @JsonIgnore
    @AssertTrue(message = WalletValidationMsg.INFO_POINT_CHANGE_AMOUNT_NOT_ZERO)
    public boolean isChangeAmountNonZero() {
        return changeAmount == null || changeAmount != 0;
    }
}
