package com.oriole.wisepen.market.api.domain.dto.req;

import com.oriole.wisepen.market.api.domain.base.InfoPointBase;
import com.oriole.wisepen.market.api.enums.InfoPointChangeType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public class InfoPointChangeRequest extends InfoPointBase {

    // 订单id
    private Long relatedId;

    // 交易类型
    private InfoPointChangeType changeType;

    // Json格式的备注
    private String meta;
    private Long operatorId;
}
