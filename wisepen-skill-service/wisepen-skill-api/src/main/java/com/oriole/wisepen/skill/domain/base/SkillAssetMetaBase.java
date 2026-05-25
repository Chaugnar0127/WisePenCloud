package com.oriole.wisepen.skill.domain.base;

import lombok.Data;

@Data
public class SkillAssetMetaBase {
    private String path;
    private String objectKey;
    private String kind;
    private Long sizeBytes;
}
