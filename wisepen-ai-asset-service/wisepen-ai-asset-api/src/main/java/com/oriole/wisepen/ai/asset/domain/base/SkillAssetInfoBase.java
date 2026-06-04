package com.oriole.wisepen.ai.asset.domain.base;

import com.oriole.wisepen.ai.asset.enums.SkillAssetUploadStatus;
import com.oriole.wisepen.ai.asset.enums.SkillAssetResourceType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class SkillAssetInfoBase {
    private String id;
    private String name;
    private String path;
    private String objectKey;
    private SkillAssetResourceType skillAssetResourceType;
    private SkillAssetUploadStatus uploadStatus;
    private Long size;
}
