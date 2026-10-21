package com.oriole.wisepen.web.app.domain.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleHtmlPageUploadMeta {
    private String filename;
    private Long uploaderId;
    private Long expectedSize;
    private String expectedMd5;
    private String mountTargetTagId;
}
