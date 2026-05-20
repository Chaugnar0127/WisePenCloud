package com.oriole.wisepen.note.api.domain.dto.req;

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

    @NotBlank(message = "原资源 ID 不能为空")
    private String originalResourceId;

    @NotBlank(message = "新资源 ID 不能为空")
    private String newResourceId;

    @NotNull(message = "新拥有者 ID 不能为空")
    private Long newOwnerId;

    private Long targetVersion;
}
