package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.domain.base.ResourceItemInfoBase;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResourceForkRequest extends ResourceItemInfoBase {
    @NotBlank(message = ResourceValidationMsg.SOURCE_RESOURCE_ID_NOT_BLANK)
    private String sourceResourceId;

    /** 笔记快照版本，空表示 fork 最新 */
    private String version;
}
