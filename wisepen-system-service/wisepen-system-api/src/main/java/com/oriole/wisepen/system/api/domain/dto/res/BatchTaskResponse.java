package com.oriole.wisepen.system.api.domain.dto.res;

import com.oriole.wisepen.system.api.enums.BatchTaskStatus;
import com.oriole.wisepen.system.api.enums.BatchTaskType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class BatchTaskResponse {
    private Long taskId;
    private BatchTaskType type;
    private String name;
    private BatchTaskStatus status;
    private Map<String, Object> meta;
    private Long operatorUserId;
    private long totalCount;
    private long pendingCount;
    private long runningCount;
    private long successCount;
    private long failedCount;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
}
