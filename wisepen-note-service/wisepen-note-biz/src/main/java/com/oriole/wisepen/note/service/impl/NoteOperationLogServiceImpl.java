package com.oriole.wisepen.note.service.impl;

import com.oriole.wisepen.note.api.domain.dto.res.NoteOperationLogResponse;
import com.oriole.wisepen.note.api.domain.mq.NoteOperationLogMessage;
import com.oriole.wisepen.note.domain.entity.NoteOperationLogEntity;
import com.oriole.wisepen.note.repository.NoteOperationLogRepository;
import com.oriole.wisepen.note.service.INoteOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteOperationLogServiceImpl implements INoteOperationLogService {

    private final NoteOperationLogRepository noteOperationLogRepository;

    @Override
    public void batchSave(NoteOperationLogMessage message) {
        List<NoteOperationLogEntity> entities = message.getEntries().stream().map(entry -> {
            NoteOperationLogEntity entity = new NoteOperationLogEntity();
            entity.setResourceId(message.getResourceId());
            entity.setUserId(entry.getUserId());
            entity.setOperationType(entry.getOperationType());
            if (entry.getUpdateData() != null) {
                entity.setUpdateData(new Binary(Base64.getDecoder().decode(entry.getUpdateData())));
            }
            entity.setContentSummary(entry.getContentSummary());
            entity.setTimestamp(new Date(entry.getTimestamp()));
            entity.setMergedCount(entry.getMergedCount());
            entity.setDetails(entry.getDetails());
            return entity;
        }).toList();

        noteOperationLogRepository.saveAll(entities);
    }

    @Override
    public Page<NoteOperationLogResponse> listOperationLogs(String resourceId, int page, int size) {
        Page<NoteOperationLogEntity> entities = noteOperationLogRepository
                .findByResourceIdOrderByTimestampDesc(resourceId, PageRequest.of(page - 1, size));
        return entities.map(e -> {
            NoteOperationLogResponse resp = new NoteOperationLogResponse();
            resp.setId(e.getId());
            resp.setUserId(e.getUserId());
            resp.setOperationType(e.getOperationType());
            resp.setContentSummary(e.getContentSummary());
            resp.setTimestamp(e.getTimestamp());
            resp.setMergedCount(e.getMergedCount());
            return resp;
        });
    }

    @Override
    public void deleteByResourceId(String resourceId) {
        noteOperationLogRepository.deleteByResourceId(resourceId);
    }
}
