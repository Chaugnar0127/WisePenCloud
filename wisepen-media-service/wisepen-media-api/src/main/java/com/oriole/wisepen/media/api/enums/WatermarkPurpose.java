package com.oriole.wisepen.media.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WatermarkPurpose {
    PREVIEW(1, "PREVIEW"),
    PLAYBACK(2, "PLAYBACK"),
    /** 历史取证水印下载会话用途，保留用于读取旧会话记录。 */
    DOWNLOAD(3, "DOWNLOAD");

    private final int code;

    @EnumValue
    @JsonValue
    private final String value;
}
