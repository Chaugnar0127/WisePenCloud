package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.constant.WalletValidationMsg;
import com.oriole.wisepen.user.api.domain.base.InfoPointBase;
import com.oriole.wisepen.user.api.enums.InfoPointChangeType;
import com.oriole.wisepen.user.api.enums.InfoPointTradeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class InfoPointChangeRequest extends InfoPointBase {

    private Long relatedId;

    @NotNull(message = WalletValidationMsg.INFO_POINT_CHANGE_TYPE_NOT_NULL)
    private InfoPointChangeType changeType;

    private InfoPointTradeStatus tradeStatus;
    private String meta;
    private Long operatorId;
}
