package com.oriole.wisepen.note.api.domain.dto.req;

import lombok.Data;

@Data
public class NoteVersionQueryRequest {
    private int page = 1;
    private int size = 20;
}
