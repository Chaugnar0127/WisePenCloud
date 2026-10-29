package com.oriole.wisepen.system.service;

public interface BatchTaskExecutionService {

    ExecutionSummary executePendingItems();

    void reconcileTaskStatus(Long taskId);

    record ExecutionSummary(int processed, int succeeded, int failed, int interrupted) {
    }
}
