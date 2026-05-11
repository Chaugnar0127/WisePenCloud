package com.oriole.wisepen.document.api.domain.dto.req;

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

    @NotBlank(message = "原资源 ID 不能为空")
    private String originalResourceId;

    @NotBlank(message = "新资源 ID 不能为空")
    private String newResourceId;

    @NotNull(message = "新拥有者 ID 不能为空")
    private Long newOwnerId;

    @NotBlank(message = "新的源文件 ObjectKey 不能为空")
    private String newSourceObjectKey;

    // 有些资源可能没有生成预览，所以允许为空
    private String newPreviewObjectKey;
}
