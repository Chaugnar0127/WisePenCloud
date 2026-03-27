package com.oriole.wisepen.note.api.domain.dto.res;

import lombok.Data;

import java.util.Date;

@Data
public class NoteOperationLogResponse {
    private String id;
    private String userId;
    private String operationType;
    private String contentSummary;
    private Date timestamp;
    private Integer mergedCount;
}
