package com.oriole.wisepen.media.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MediaStatusEnum {
    /** 文件正在上传到对象存储。 */
    UPLOADING(0, "UPLOADING"),

    /** 文件已上传完成，等待媒体处理任务消费。 */
    UPLOADED(1, "UPLOADED"),

    /** 正在读取媒体基础信息，如尺寸、时长或流信息。 */
    PROBING(2, "PROBING"),

    /** 正在生成媒体基础产物，如预览图、封面或 HLS。 */
    PACKAGING(3, "PACKAGING"),

    /** 历史状态：正在进行取证水印预处理或能力确认，新上传媒体不再进入该状态。 */
    FORENSIC_PREPROCESSING(4, "FORENSIC_PREPROCESSING"),

    /** 正在向资源服务注册可对外访问的资源。 */
    REGISTERING_RES(5, "REGISTERING_RES"),

    /** 媒体处理和资源注册已完成，可预览、播放或下载。 */
    READY(6, "READY"),

    /** 文件上传传输超时。 */
    TRANSFER_TIMEOUT(-1, "TRANSFER_TIMEOUT"),

    /** 媒体产物已生成，但资源注册超时或失败。 */
    REGISTERING_RES_TIMEOUT(-2, "REGISTERING_RES_TIMEOUT"),

    /** 媒体处理链路失败，需用户触发重试。 */
    FAILED(-3, "FAILED");

    private final int code;

    @EnumValue
    @JsonValue
    private final String value;
}
