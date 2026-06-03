package com.oriole.wisepen.ai.asset.domain.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oriole.wisepen.ai.asset.constant.SkillValidationMsg;
import com.oriole.wisepen.ai.asset.enums.SkillSourceTypeEnum;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillCreateRequest {
    private String name;

    private String description;

    @Builder.Default
    private SkillSourceTypeEnum sourceType = SkillSourceTypeEnum.MANUAL;

    @AssertTrue(message = SkillValidationMsg.SKILL_NAME_NOT_BLANK)
    @JsonIgnore
    public boolean isNamePresent() {
        return StringUtils.hasText(name);
    }
}
