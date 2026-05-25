package com.oriole.wisepen.skill.domain.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SkillVersionEntity {
    private String version;
    private String skillMdObjectKey;
    private Boolean published = false;
    private Boolean enabled = false;
    private List<SkillAssetEntity> assetsManifest = new ArrayList<>();
}
