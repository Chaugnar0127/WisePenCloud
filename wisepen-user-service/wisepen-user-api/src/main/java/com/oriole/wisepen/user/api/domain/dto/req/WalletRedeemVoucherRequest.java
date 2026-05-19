package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.constant.WalletValidationMsg;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class WalletRedeemVoucherRequest implements Serializable {
	@NotBlank(message = WalletValidationMsg.VOUCHER_CODE_NOT_BLANK)
	private String voucherCode;
}
