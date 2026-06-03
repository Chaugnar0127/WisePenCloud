package com.oriole.wisepen.ai.asset.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillVersionEntity {
    private String version;
    private String skillMdObjectKey;
    @Builder.Default
    private Boolean published = false;
    @Builder.Default
    private Boolean enabled = false;
    @Builder.Default
    private List<SkillAssetEntity> assetsManifest = new ArrayList<>();
}
