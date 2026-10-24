package com.oriole.wisepen.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.system.api.domain.dto.req.BatchTaskCreateRequest;
import com.oriole.wisepen.system.api.domain.dto.res.BatchTaskItemResponse;
import com.oriole.wisepen.system.api.domain.dto.res.BatchTaskResponse;
import com.oriole.wisepen.system.api.enums.BatchTaskItemStatus;
import com.oriole.wisepen.system.api.enums.BatchTaskStatus;
import com.oriole.wisepen.system.domain.entity.BatchTaskEntity;
import com.oriole.wisepen.system.domain.entity.BatchTaskItemEntity;
import com.oriole.wisepen.system.excpetion.SysError;
import com.oriole.wisepen.system.mapper.BatchTaskItemMapper;
import com.oriole.wisepen.system.mapper.BatchTaskMapper;
import com.oriole.wisepen.system.service.BatchTaskExecutionService;
import com.oriole.wisepen.system.service.BatchTaskService;
import com.oriole.wisepen.system.service.batch.BatchTaskExecutor;
import com.oriole.wisepen.system.service.batch.BatchTaskExecutorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchTaskServiceImpl implements BatchTaskService {

    private static final int INSERT_BATCH_SIZE = 500;

    private final BatchTaskMapper batchTaskMapper;
    private final BatchTaskItemMapper batchTaskItemMapper;
    private final BatchTaskExecutionService batchTaskExecutionService;
    private final BatchTaskExecutorRegistry executorRegistry;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Long createTask(BatchTaskCreateRequest request, Long operatorUserId) {

        BatchTaskEntity task = new BatchTaskEntity();
        BeanUtil.copyProperties(request, task, CopyOptions.create().setIgnoreProperties("meta"));

        task.setName(request.getName().trim());
        task.setStatus(BatchTaskStatus.CREATED);
        task.setOperatorUserId(operatorUserId);

        try {
            task.setMeta(objectMapper.writeValueAsString(request.getMeta()));
        } catch (JsonProcessingException e) {
            throw new ServiceException(SysError.BATCH_TASK_META_INVALID);
        }

        // 分解子任务
        BatchTaskExecutor executor = executorRegistry.get(request.getType());
        List<BatchTaskItemEntity> items = executor.expandTask(task);

        if (task.getMeta() == null || items == null || items.isEmpty()) {
            throw new ServiceException(SysError.BATCH_TASK_META_INVALID);
        }

        Long taskId = transactionTemplate.execute(status -> {
            batchTaskMapper.insert(task);
            LocalDateTime now = LocalDateTime.now();
            for (BatchTaskItemEntity item : items) {
                item.setTaskId(task.getId());
                if (item.getStatus() == BatchTaskItemStatus.FAILED) item.setFinishTime(now);
                item.setCreateTime(now);
                item.setUpdateTime(now);
            }
            // 批量插入 BatchTaskItem 表
            for (int start = 0; start < items.size(); start += INSERT_BATCH_SIZE) {
                batchTaskItemMapper.insertBatch(items.subList(
                        start, Math.min(start + INSERT_BATCH_SIZE, items.size())));
            }
            return task.getId();
        });
        log.info("batch task created. taskId={} type={} operatorUserId={} items={}",
                taskId, request.getType(), operatorUserId, items.size());
        return taskId;
    }

    @Override
    public void startTask(Long taskId) {
        BatchTaskEntity task = getTaskEntity(taskId);
        // 任务已经在运行或等待调度
        if (task.getStatus() == BatchTaskStatus.PENDING || task.getStatus() == BatchTaskStatus.RUNNING) {
            return;
        }

        BatchTaskStatus targetStatus;
        if (task.getStatus() == BatchTaskStatus.CREATED) { // 如原状态是 CREATED
            targetStatus = BatchTaskStatus.PENDING;// 转为 PENDING
        } else if (task.getStatus() == BatchTaskStatus.PAUSING
                || task.getStatus() == BatchTaskStatus.PAUSED) { // 如原状态是 PAUSING 或 PAUSED
            targetStatus = BatchTaskStatus.RUNNING;// 转为 RUNNING
        } else {
            throw new ServiceException(SysError.BATCH_TASK_STATE_INVALID); // 否则报错
        }
        // 更新状态
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<BatchTaskEntity> updateWrapper = Wrappers.<BatchTaskEntity>lambdaUpdate()
                .eq(BatchTaskEntity::getId, taskId)
                .isNull(BatchTaskEntity::getDeletedAt)
                .set(BatchTaskEntity::getStatus, targetStatus)
                .set(BatchTaskEntity::getUpdateTime, now);
        if (task.getStatus() == BatchTaskStatus.CREATED) {
            updateWrapper.eq(BatchTaskEntity::getStatus, BatchTaskStatus.CREATED)
                    .set(BatchTaskEntity::getStartTime, now);
        } else {
            // PAUSING 可能在更新前收敛为 PAUSED，恢复操作同时接受这两个来源状态
            updateWrapper.in(BatchTaskEntity::getStatus,
                    BatchTaskStatus.PAUSING, BatchTaskStatus.PAUSED);
        }

        int updated = batchTaskMapper.update(null, updateWrapper);
        if (updated == 0) {
            // 可能是并发，进行检查
            BatchTaskStatus currentStatus = getTaskEntity(taskId).getStatus();
            if (currentStatus != BatchTaskStatus.PENDING && currentStatus != BatchTaskStatus.RUNNING) {
                throw new ServiceException(SysError.BATCH_TASK_STATE_INVALID);
            }
        } else {
            log.info("batch task status changed. taskId={} from={} to={}",
                    taskId, task.getStatus(), targetStatus);
        }
        batchTaskExecutionService.reconcileTaskStatus(taskId);
    }

    @Override
    public void stopTask(Long taskId) {
        BatchTaskEntity task = getTaskEntity(taskId);
        // 如任务处在 PAUSING 或 PAUSED，则仅刷新状态
        if (task.getStatus() == BatchTaskStatus.PAUSING || task.getStatus() == BatchTaskStatus.PAUSED) {
            batchTaskExecutionService.reconcileTaskStatus(taskId);
            return;
        }
        // 不能停止一个不在 PENDING 也不在 RUNNING 的任务
        if (task.getStatus() != BatchTaskStatus.PENDING && task.getStatus() != BatchTaskStatus.RUNNING) {
            throw new ServiceException(SysError.BATCH_TASK_STATE_INVALID);
        }

        // 更新状态
        LocalDateTime now = LocalDateTime.now();
        int updated = batchTaskMapper.update(null, Wrappers.<BatchTaskEntity>lambdaUpdate()
                .eq(BatchTaskEntity::getId, taskId)
                .in(BatchTaskEntity::getStatus, BatchTaskStatus.PENDING, BatchTaskStatus.RUNNING)
                .isNull(BatchTaskEntity::getDeletedAt)
                .set(BatchTaskEntity::getStatus, BatchTaskStatus.PAUSING)
                .set(BatchTaskEntity::getUpdateTime, now));
        if (updated == 0) {
            // 可能是并发，进行检查
            BatchTaskStatus currentStatus = getTaskEntity(taskId).getStatus();
            if (currentStatus != BatchTaskStatus.PAUSING && currentStatus != BatchTaskStatus.PAUSED) {
                throw new ServiceException(SysError.BATCH_TASK_STATE_INVALID);
            }
        } else {
            log.info("batch task status changed. taskId={} from={} to={}",
                    taskId, task.getStatus(), BatchTaskStatus.PAUSING);
        }
        batchTaskExecutionService.reconcileTaskStatus(taskId);
    }

    @Override
    public void deleteTask(Long taskId, Long operatorUserId) {
        BatchTaskEntity task = getTaskEntity(taskId);
        // 任务是否已经结束 或 刚创建 或 已经被成功暂停
        boolean deletable = task.getStatus() == BatchTaskStatus.CREATED
                || task.getStatus() == BatchTaskStatus.PAUSED
                || task.getStatus().isTerminal();
        if (!deletable) { // 不在上述状态不能删除
            throw new ServiceException(SysError.BATCH_TASK_STATE_INVALID);
        }

        // 更新状态
        LocalDateTime now = LocalDateTime.now();
        int updated = batchTaskMapper.update(null, Wrappers.<BatchTaskEntity>lambdaUpdate()
                .eq(BatchTaskEntity::getId, taskId)
                .eq(BatchTaskEntity::getStatus, task.getStatus())
                .isNull(BatchTaskEntity::getDeletedAt)
                .set(BatchTaskEntity::getDeletedAt, now)
                .set(BatchTaskEntity::getDeletedBy, operatorUserId)
                .set(BatchTaskEntity::getUpdateTime, now));
        if (updated == 0) {
            throw new ServiceException(SysError.BATCH_TASK_STATE_INVALID);
        }
        log.info("batch task deleted. taskId={} status={} operatorUserId={}",
                taskId, task.getStatus(), operatorUserId);
    }

    @Override
    public BatchTaskResponse getTaskInfo(Long taskId) {
        BatchTaskEntity task = getTaskEntity(taskId);

        BatchTaskResponse response = BeanUtil.copyProperties(task, BatchTaskResponse.class, "meta");
        response.setTaskId(task.getId());
        response.setMeta(readJson(task.getMeta()));

        // 计算统计信息
        long pendingCount = countItems(taskId, BatchTaskItemStatus.PENDING);
        long runningCount = countItems(taskId, BatchTaskItemStatus.RUNNING);
        long successCount = countItems(taskId, BatchTaskItemStatus.SUCCEEDED);
        long failedCount = countItems(taskId, BatchTaskItemStatus.FAILED);
        response.setTotalCount(pendingCount + runningCount + successCount + failedCount);
        response.setPendingCount(pendingCount);
        response.setRunningCount(runningCount);
        response.setSuccessCount(successCount);
        response.setFailedCount(failedCount);
        return response;
    }

    @Override
    public PageR<BatchTaskItemResponse> listTaskItems(Long taskId, BatchTaskItemStatus status, int page, int size) {
        getTaskEntity(taskId);
        LambdaQueryWrapper<BatchTaskItemEntity> wrapper = Wrappers.<BatchTaskItemEntity>lambdaQuery()
                .eq(BatchTaskItemEntity::getTaskId, taskId)
                .eq(status != null, BatchTaskItemEntity::getStatus, status)
                .orderByAsc(BatchTaskItemEntity::getId);

        IPage<BatchTaskItemEntity> result = batchTaskItemMapper.selectPage(new Page<>(page, size), wrapper);
        PageR<BatchTaskItemResponse> response = new PageR<>(result.getTotal(), page, size);

        response.addAll(result.getRecords().stream().map(item -> {
            BatchTaskItemResponse itemResponse = BeanUtil.copyProperties(
                    item, BatchTaskItemResponse.class, "meta", "result");
            itemResponse.setItemId(item.getId());
            itemResponse.setMeta(readJson(item.getMeta()));
            itemResponse.setResult(readJson(item.getResult()));
            return itemResponse;
        }).toList());

        return response;
    }

    private BatchTaskEntity getTaskEntity(Long taskId) {
        BatchTaskEntity task = batchTaskMapper.selectOne(Wrappers.<BatchTaskEntity>lambdaQuery()
                .eq(BatchTaskEntity::getId, taskId)
                .isNull(BatchTaskEntity::getDeletedAt));
        if (task == null) throw new ServiceException(SysError.BATCH_TASK_NOT_FOUND);
        return task;
    }

    private long countItems(Long taskId, BatchTaskItemStatus status) {
        return batchTaskItemMapper.selectCount(Wrappers.<BatchTaskItemEntity>lambdaQuery()
                .eq(BatchTaskItemEntity::getTaskId, taskId)
                .eq(BatchTaskItemEntity::getStatus, status));
    }

    private Map<String, Object> readJson(String value) {
        if (value == null) return Collections.emptyMap();
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new ServiceException(SysError.BATCH_TASK_META_INVALID);
        }
    }
}
