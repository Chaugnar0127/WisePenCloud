package com.oriole.wisepen.system.service.batch;

import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.system.api.enums.BatchTaskType;
import com.oriole.wisepen.system.excpetion.SysError;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class BatchTaskExecutorRegistry {

    private final Map<BatchTaskType, BatchTaskExecutor> executors = new EnumMap<>(BatchTaskType.class);

    public BatchTaskExecutorRegistry(List<BatchTaskExecutor> registeredExecutors) {
        for (BatchTaskExecutor executor : registeredExecutors) {
            BatchTaskExecutor existing = executors.putIfAbsent(executor.getType(), executor);
            if (existing != null) {
                throw new IllegalStateException("Duplicate batch task executor type: " + executor.getType());
            }
        }
    }

    public BatchTaskExecutor get(BatchTaskType type) {
        BatchTaskExecutor executor = executors.get(type);
        if (executor == null) {
            throw new ServiceException(SysError.BATCH_TASK_TYPE_UNSUPPORTED);
        }
        return executor;
    }
}
