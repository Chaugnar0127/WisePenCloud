package com.oriole.wisepen.document.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentStatusEnum {

    UPLOADING(0),
    UPLOADED(1),
    CONVERTING_AND_PARSING(2),
    REGISTERING_RES(3),
    READY(4),

    /** 上传超时：OSS 回调在预期时限内未收到，需人工或自动重试 */
    TRANSFER_TIMEOUT(-1),
    REGISTERING_RES_TIMEOUT(-2),
    FAILED(-3);

    @EnumValue
    private final int code;
}