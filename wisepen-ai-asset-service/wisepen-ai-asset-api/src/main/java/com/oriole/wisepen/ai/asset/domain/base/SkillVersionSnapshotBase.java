package com.oriole.wisepen.ai.asset.domain.base;

import com.oriole.wisepen.ai.asset.enums.SkillVersionStatusEnum;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class SkillVersionSnapshotBase {
    private String resourceId;
    private Integer version;
    private SkillVersionStatusEnum status;
    @Builder.Default
    private List<SkillFileBase> files = new ArrayList<>();
}
