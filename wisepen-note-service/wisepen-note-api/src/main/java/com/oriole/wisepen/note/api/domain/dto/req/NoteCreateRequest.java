package com.oriole.wisepen.note.api.domain.dto.req;

import com.oriole.wisepen.note.api.constant.NoteValidationMsg;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteCreateRequest {
    @NotBlank(message = NoteValidationMsg.NOTE_TITLE_NOT_BLANK)
    private String title;
}
