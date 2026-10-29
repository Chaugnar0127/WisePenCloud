package com.oriole.wisepen.system.api.domain.dto.res;

import com.oriole.wisepen.system.api.enums.BatchTaskItemStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class BatchTaskItemResponse {
    private Long itemId;
    private Long taskId;
    private BatchTaskItemStatus status;
    private Map<String, Object> meta;
    private Map<String, Object> result;
    private Integer errorCode;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
}
