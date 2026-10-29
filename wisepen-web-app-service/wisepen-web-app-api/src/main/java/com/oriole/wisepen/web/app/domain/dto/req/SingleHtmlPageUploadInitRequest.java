package com.oriole.wisepen.web.app.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.oriole.wisepen.web.app.constant.WebAppValidationMsg.EXPECTED_SIZE_NOT_NULL;
import static com.oriole.wisepen.web.app.constant.WebAppValidationMsg.FILENAME_NOT_BLANK;
import static com.oriole.wisepen.web.app.constant.WebAppValidationMsg.MD5_NOT_BLANK;
import static com.oriole.wisepen.web.app.constant.WebAppValidationMsg.RESOURCE_ID_NOT_BLANK;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleHtmlPageUploadInitRequest {
    private String resourceId;

    @NotBlank(message = FILENAME_NOT_BLANK)
    private String filename;

    @NotBlank(message = MD5_NOT_BLANK)
    private String md5;

    @NotNull(message = EXPECTED_SIZE_NOT_NULL)
    @Positive(message = EXPECTED_SIZE_NOT_NULL)
    private Long expectedSize;

    private String mountTargetTagId;

}
