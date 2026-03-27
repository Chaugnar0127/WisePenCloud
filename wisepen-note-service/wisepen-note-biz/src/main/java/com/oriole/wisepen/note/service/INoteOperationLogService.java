package com.oriole.wisepen.note.service;

import com.oriole.wisepen.note.api.domain.dto.res.NoteOperationLogResponse;
import com.oriole.wisepen.note.api.domain.mq.NoteOperationLogMessage;
import org.springframework.data.domain.Page;

public interface INoteOperationLogService {

    void batchSave(NoteOperationLogMessage message);

    Page<NoteOperationLogResponse> listOperationLogs(String resourceId, int page, int size);

    void deleteByResourceId(String resourceId);
}
