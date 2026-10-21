package com.oriole.wisepen.web.app.domain.base;

import com.oriole.wisepen.web.app.enums.SingleHtmlPageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SingleHtmlPageVersionBase {
    private SingleHtmlPageUploadMeta uploadMeta;
    private SingleHtmlPageStatus singleHtmlPageStatus;
    private Long size;
    private String contentHash;
}
