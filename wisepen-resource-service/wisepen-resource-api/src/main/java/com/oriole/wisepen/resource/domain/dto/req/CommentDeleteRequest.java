package com.oriole.wisepen.resource.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentDeleteRequest {
    @NotBlank
    private String resourceId;

    @NotBlank
    private String commentId;
}
