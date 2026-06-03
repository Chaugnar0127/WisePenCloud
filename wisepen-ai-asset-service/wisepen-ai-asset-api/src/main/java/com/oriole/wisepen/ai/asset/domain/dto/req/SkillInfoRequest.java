package com.oriole.wisepen.ai.asset.domain.dto.req;

import com.oriole.wisepen.ai.asset.domain.base.SkillInfoBase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SkillInfoRequest extends SkillInfoBase {
    private String resourceId;
    private String storageBizTag;
}
