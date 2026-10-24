package com.oriole.wisepen.system.service.batch;

import com.oriole.wisepen.system.api.enums.BatchTaskType;
import com.oriole.wisepen.system.domain.entity.BatchTaskEntity;
import com.oriole.wisepen.system.domain.entity.BatchTaskItemEntity;

import java.util.List;

public interface BatchTaskExecutor {

    BatchTaskType getType();

    List<BatchTaskItemEntity> expandTask(BatchTaskEntity task);

    BatchTaskItemEntity executeItem(BatchTaskItemEntity taskItem);
}
