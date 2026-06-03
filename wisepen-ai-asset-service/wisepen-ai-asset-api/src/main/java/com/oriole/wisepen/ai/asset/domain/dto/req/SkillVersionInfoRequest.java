package com.oriole.wisepen.ai.asset.domain.dto.req;

import com.oriole.wisepen.ai.asset.domain.base.SkillVersionSnapshotBase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SkillVersionInfoRequest extends SkillVersionSnapshotBase {
}
