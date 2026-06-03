package com.oriole.wisepen.ai.asset.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillFileEntity {
    private String id;
    private String name;
    private String path;
    private String objectKey;
}
