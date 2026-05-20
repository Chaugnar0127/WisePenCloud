package com.oriole.wisepen.user.api.domain.dto.res;

import com.oriole.wisepen.user.api.enums.InfoPointChangeType;
import com.oriole.wisepen.user.api.enums.InfoPointTradeStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InfoPointTradeRelatedIdResponse {
    private Long relatedId;
    private Long matchedRecordId;
    private Long userId;
    private Integer changeAmount;
    private InfoPointChangeType changeType;
    private InfoPointTradeStatus tradeStatus;
    private LocalDateTime createTime;
}
