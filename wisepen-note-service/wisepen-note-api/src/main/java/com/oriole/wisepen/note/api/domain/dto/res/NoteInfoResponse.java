package com.oriole.wisepen.note.api.domain.dto.res;

import lombok.Data;

import java.util.Date;

@Data
public class NoteInfoResponse {
    private String resourceId;
    private Long version;
    private Date lastUpdatedAt;
    private String lastUpdatedBy;
}
