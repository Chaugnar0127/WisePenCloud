package com.oriole.wisepen.ai.asset.domain.entity;

import com.oriole.wisepen.ai.asset.enums.SkillVersionStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wisepen_skill_versions")
@CompoundIndexes({
        @CompoundIndex(name = "uk_resource_version", def = "{'resourceId': 1, 'version': 1}", unique = true)
})
public class SkillVersionSnapshotEntity {
    @Id
    private String id;

    private String resourceId;

    private Integer version;

    @Builder.Default
    private List<SkillFileEntity> files = new ArrayList<>();

    @Builder.Default
    private SkillVersionStatusEnum status = SkillVersionStatusEnum.DRAFT;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;
}
