package com.oriole.wisepen.file.storage.exception;

import com.oriole.wisepen.common.core.exception.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件模块错误码枚举
 * 范围：2000-2999
 *
 * @author Ian.xiong
 */
@Getter
@AllArgsConstructor
public enum StorageErrorCode implements IErrorCode {

    // ======== 基础校验错误 (3000-3049) ========
    PROVIDER_NOT_SUPPORTED(3001, "不支持的存储驱动类型"),
    FILE_SIZE_EXCEEDED(3002, "图床文件大小超过限制"),
    FILE_TYPE_UNSUPPORTED(3003, "不支持的图片格式"),
    INVALID_URL_FORMAT(3004, "无效的永久 URL 格式"),

    // ======== 流程与安全错误 (3050-3099) ========
    RECORD_NOT_FOUND(3050, "文件物理记录不存在"),
    CALLBACK_SIGNATURE_INVALID(3051, "非法回调请求，签名校验失败"),
    STS_TOKEN_GENERATE_FAILED(3052, "STS临时凭证生成失败"),
    CALLBACK_POLICY_GENERATE_FAILED(3053, "直传回调策略生成失败"),

    // ======== 底层读写操作错误 (3100-3199) ========
    FILE_UPLOAD_ERROR(3100, "文件物理推流上传失败"),
    FILE_DOWNLOAD_ERROR(3101, "获取文件下载直链失败"),
    FILE_DELETE_ERROR(3102, "物理文件删除失败"),
    FILE_COPY_ERROR(3103, "物理文件拷贝（秒传）失败"),
    FILE_READ_ERROR(3104, "文件读取失败");

    private final Integer code;
    private final String msg;
}
