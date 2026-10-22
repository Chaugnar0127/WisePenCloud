package com.oriole.wisepen.ai.asset.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AssetUploadStatus {
    // 存在尚未提交的上传，新旧资产均不可在此状态下发布
    UPLOADING(1, "UPLOADING"),
    // 正式文件可用且不存在待提交上传
    AVAILABLE(2, "AVAILABLE");

    private final int code;

    @EnumValue
    @JsonValue
    private final String value;
}
