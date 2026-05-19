package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.constant.GroupValidationMsg;
import com.oriole.wisepen.user.api.constant.WalletValidationMsg;
import com.oriole.wisepen.user.api.enums.TokenTransferType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalletTransferTokenRequest {
	@NotNull(message = GroupValidationMsg.GROUP_ID_NOT_NULL)
	private Long groupId;

	@NotNull(message = WalletValidationMsg.TOKEN_COUNT_NOT_NULL)
	@Min(value = 1, message = WalletValidationMsg.TOKEN_COUNT_MIN)
	private Integer tokenCount;

	TokenTransferType tokenTransferType;
}
