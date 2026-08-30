package com.oriole.wisepen.questionnaire.api.domain.dto.res;

import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireInfoResponse {
    private ResourceItemResponse resourceInfo;
    private String resourceId;
    private Integer version;
    private Integer draftVersion;
    private String title;
    private String description;
    private LocalDateTime updateTime;
}
