package com.oriole.wisepen.ai.asset.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillAssetEntity {
    private String path;
    private String objectKey;
    private String kind;
    private Long sizeBytes;
}
