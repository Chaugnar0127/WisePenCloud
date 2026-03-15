package com.oriole.wisepen.document.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentStatusEnum {

    UPLOADING(0),
    /** 物理文件已落地 OSS，等待解析流水线处理 */
    UPLOADED(1),
    CONVERTING(2),
    READY(3),
    /** 上传超时：OSS 回调在预期时限内未收到，需人工或自动重试 */
    TRANSFER_TIMEOUT(-1),
    FAILED(-2);

    @EnumValue
    private final int code;
}