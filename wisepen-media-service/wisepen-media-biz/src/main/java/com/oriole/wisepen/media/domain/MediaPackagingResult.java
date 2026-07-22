package com.oriole.wisepen.media.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MediaPackagingResult {

    private String sourceHlsPrefix;

    private List<String> sourceHlsObjectKeys;

    private String previewObjectKey;

    private String posterObjectKey;

    private Long durationMs;

    private Integer width;

    private Integer height;
}
