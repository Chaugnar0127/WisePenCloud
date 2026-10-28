package com.oriole.wisepen.web.app.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.oriole.wisepen.web.app.constant.WebAppValidationMsg.TITLE_NOT_BLANK;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleHtmlPageCreateRequest {
    @NotBlank(message = TITLE_NOT_BLANK)
    private String title;

    private String mountTargetTagId;
}
