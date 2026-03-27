package com.oriole.wisepen.note.api.domain.dto.res;

import lombok.Data;

import java.util.Date;

@Data
public class NoteVersionListResponse {
    private String id;
    private Long version;
    private String type;
    private String label;
    private Date createdAt;
    private String createdBy;
}
