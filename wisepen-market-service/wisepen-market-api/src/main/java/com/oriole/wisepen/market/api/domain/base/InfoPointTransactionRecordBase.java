package com.oriole.wisepen.market.api.domain.base;

import com.oriole.wisepen.market.api.enums.InfoPointChangeType;
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
    private Long relatedId;
    private Integer balanceAfter;
    private String meta;
    private LocalDateTime createTime;
}
