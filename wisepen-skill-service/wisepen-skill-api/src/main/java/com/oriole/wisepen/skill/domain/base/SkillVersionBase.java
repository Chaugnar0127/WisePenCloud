package com.oriole.wisepen.skill.domain.base;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SkillVersionBase {
    private String version;
    private String skillMdObjectKey;
    private Boolean published;
    private Boolean enabled;
    private List<SkillAssetMetaBase> assetsManifest = new ArrayList<>();
}
