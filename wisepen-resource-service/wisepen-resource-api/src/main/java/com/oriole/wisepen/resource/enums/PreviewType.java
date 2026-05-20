package com.oriole.wisepen.resource.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PreviewType {
    NONE(0, "无预览"),
    WATERMARK(1, "水印预览"),
    FULL(2, "完整预览");

    private final int code;
    private final String desc;
}
