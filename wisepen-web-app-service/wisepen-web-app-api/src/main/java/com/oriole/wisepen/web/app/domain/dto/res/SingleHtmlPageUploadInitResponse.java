package com.oriole.wisepen.web.app.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleHtmlPageUploadInitResponse {
    private String singleHtmlPageId;
    private Integer version;
    private String objectKey;
    private String putUrl;
    private String callbackHeader;
    private Boolean flashUploaded;
}
