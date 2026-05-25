package com.oriole.wisepen.skill.domain.dto;

import com.oriole.wisepen.skill.constant.SkillValidationMsg;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillAssetUploadInitReqDTO {
    @NotBlank(message = SkillValidationMsg.SKILL_ID_NOT_BLANK)
    private String skillId;

    @NotBlank(message = SkillValidationMsg.SKILL_VERSION_NOT_BLANK)
    private String version;

    @NotBlank(message = SkillValidationMsg.SKILL_RELATIVE_PATH_NOT_BLANK)
    private String relativePath;

    private String kind;

    private String md5;

    private Long expectedSize;
}
