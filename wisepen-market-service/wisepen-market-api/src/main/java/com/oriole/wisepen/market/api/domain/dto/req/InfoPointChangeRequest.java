package com.oriole.wisepen.market.api.domain.dto.req;

import com.oriole.wisepen.market.api.domain.base.InfoPointChangeBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public class InfoPointChangeRequest extends InfoPointChangeBase {

    //订单id
    private String relatedId;

    //Json格式的备注
    private String meta;
    private Long operatorId;
}
