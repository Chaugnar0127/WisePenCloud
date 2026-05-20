package com.oriole.wisepen.document.api.domain.dto.req;

import com.oriole.wisepen.document.api.constant.DocumentValidationMsg;
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
public class DocumentForkReqDTO {

    @NotBlank(message = DocumentValidationMsg.ORIGINAL_RESOURCE_ID_EMPTY)
    private String originalResourceId;

    @NotBlank(message = DocumentValidationMsg.NEW_RESOURCE_ID_EMPTY)
    private String newResourceId;

    @NotNull(message = DocumentValidationMsg.NEW_OWNER_ID_NULL)
    private Long newOwnerId;

    @NotBlank(message = DocumentValidationMsg.NEW_SOURCE_OBJECT_KEY_EMPTY)
    private String newSourceObjectKey;

    // 有些资源可能没有生成预览，所以允许为空
    private String newPreviewObjectKey;
}
