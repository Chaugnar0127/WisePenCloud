package com.oriole.wisepen.market.api.domain.dto.req;

import com.oriole.wisepen.market.api.domain.base.InfoPointBase;
import com.oriole.wisepen.market.api.enums.InfoPointChangeType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class InfoPointChangeRequest extends InfoPointBase {

    // 订单id
    private Long relatedId;

    // 交易类型
    @NotNull(message = "交易类型不能为空")
    private InfoPointChangeType changeType;

    // Json格式的备注
    private String meta;
    private Long operatorId;
}
