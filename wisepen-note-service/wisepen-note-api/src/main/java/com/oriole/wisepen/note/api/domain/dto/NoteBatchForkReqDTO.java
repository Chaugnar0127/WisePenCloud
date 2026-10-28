package com.oriole.wisepen.note.api.domain.dto;

import com.oriole.wisepen.note.api.domain.dto.req.NoteForkRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NoteBatchForkReqDTO extends NoteForkRequest {
    @NotBlank
    private String targetOwnerUserId;
}
