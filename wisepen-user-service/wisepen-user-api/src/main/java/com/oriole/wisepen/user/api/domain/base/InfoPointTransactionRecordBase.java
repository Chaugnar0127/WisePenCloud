package com.oriole.wisepen.user.api.domain.base;

import com.oriole.wisepen.user.api.enums.InfoPointChangeType;
import com.oriole.wisepen.user.api.enums.InfoPointTradeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InfoPointTransactionRecordBase implements Serializable {
    private Long recordId;
    private Long userId;
    private Integer changeAmount;
    private InfoPointChangeType changeType;
    private InfoPointTradeStatus tradeStatus;
    private Long relatedId;
    private Integer balanceAfter;
    private String meta;
    private LocalDateTime createTime;
}
