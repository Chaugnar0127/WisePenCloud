package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.constant.WalletValidationMsg;
import com.oriole.wisepen.user.api.enums.InfoPointTradeReverseReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfoPointTradeReverseRequest {

    @NotNull(message = WalletValidationMsg.INFO_POINT_TRADE_RELATED_ID_NOT_NULL)
    private Long relatedId;

    /** 操作人，由 Controller 从安全上下文注入；内部 Feign 冲正可不传 */
    private Long operatorId;

    @NotNull(message = WalletValidationMsg.INFO_POINT_TRADE_REVERSE_REASON_NOT_NULL)
    private InfoPointTradeReverseReason reason;

    private String detail;
}
