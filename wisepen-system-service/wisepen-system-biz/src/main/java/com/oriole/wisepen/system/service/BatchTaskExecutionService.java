package com.oriole.wisepen.system.service;

public interface BatchTaskExecutionService {

    ExecutionSummary executePendingItems();

    record ExecutionSummary(int processed, int succeeded, int failed, int interrupted) {
    }
}
