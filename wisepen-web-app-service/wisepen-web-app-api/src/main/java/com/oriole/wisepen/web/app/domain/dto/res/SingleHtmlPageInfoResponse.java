package com.oriole.wisepen.web.app.domain.dto.res;

import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleHtmlPageInfoResponse {
    private ResourceItemResponse resourceInfo;
    private Integer currentVersion;
    private SingleHtmlPageVersionInfoResponse versionInfo;
}
