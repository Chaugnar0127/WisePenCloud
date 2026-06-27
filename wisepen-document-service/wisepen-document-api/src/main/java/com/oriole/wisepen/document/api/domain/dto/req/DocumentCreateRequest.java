package com.oriole.wisepen.document.api.domain.dto.req;

import com.oriole.wisepen.document.api.constant.DocumentValidationMsg;
import com.oriole.wisepen.resource.enums.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentCreateRequest {
    @NotBlank(message = DocumentValidationMsg.DOCUMENT_TITLE_NOT_BLANK)
    private String title;
    @NotNull()
    private ResourceType resourceType;
}
