package com.oriole.wisepen.ai.asset.domain.base;

import com.oriole.wisepen.ai.asset.enums.SkillAuditStatusEnum;
import com.oriole.wisepen.ai.asset.enums.SkillSourceTypeEnum;
import com.oriole.wisepen.ai.asset.enums.SkillStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class SkillInfoBase {
    private String name;
    private String ownerId;
    private String description;
    private Integer version;
    private SkillStatusEnum skillStatus;
    private SkillAuditStatusEnum auditStatus;
    private SkillSourceTypeEnum sourceType;
}
