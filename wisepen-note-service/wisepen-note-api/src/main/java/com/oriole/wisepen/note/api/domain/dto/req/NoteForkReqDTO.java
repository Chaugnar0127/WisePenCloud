package com.oriole.wisepen.note.api.domain.dto.req;

import com.oriole.wisepen.note.api.constant.NoteValidationMsg;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteForkReqDTO {

    @NotBlank(message = NoteValidationMsg.ORIGINAL_RESOURCE_ID_NOT_BLANK)
    private String originalResourceId;

    @NotBlank(message = NoteValidationMsg.NEW_RESOURCE_ID_NOT_BLANK)
    private String newResourceId;

    @NotNull(message = NoteValidationMsg.NEW_OWNER_ID_NOT_NULL)
    private Long newOwnerId;

    private Long targetVersion;
}
