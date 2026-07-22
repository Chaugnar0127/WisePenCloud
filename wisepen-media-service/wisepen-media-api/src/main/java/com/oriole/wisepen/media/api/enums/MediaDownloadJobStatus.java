package com.oriole.wisepen.media.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MediaDownloadJobStatus {
    QUEUED(1, "QUEUED"),
    PROCESSING(2, "PROCESSING"),
    UPLOADING(3, "UPLOADING"),
    READY(4, "READY"),
    EXPIRED(5, "EXPIRED"),
    FAILED(6, "FAILED"),
    CANCELLED(7, "CANCELLED");

    private final int code;

    @EnumValue
    @JsonValue
    private final String value;
}
