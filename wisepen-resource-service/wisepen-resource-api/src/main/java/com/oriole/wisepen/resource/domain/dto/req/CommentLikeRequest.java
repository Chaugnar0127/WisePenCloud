package com.oriole.wisepen.resource.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentLikeRequest {
    @NotBlank
    private String resourceId;
    @NotBlank
    private String commentId;
}
