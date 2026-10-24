package com.oriole.wisepen.system.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.system.api.domain.dto.req.BatchTaskCreateRequest;
import com.oriole.wisepen.system.api.domain.dto.res.BatchTaskItemResponse;
import com.oriole.wisepen.system.api.domain.dto.res.BatchTaskResponse;
import com.oriole.wisepen.system.api.enums.BatchTaskItemStatus;

public interface BatchTaskService {

    BatchTaskResponse createTask(BatchTaskCreateRequest request, Long operatorUserId);

    void startTask(Long taskId);

    BatchTaskResponse getTaskInfo(Long taskId);

    PageR<BatchTaskItemResponse> listTaskItems(Long taskId, BatchTaskItemStatus status, int page, int size);
}
