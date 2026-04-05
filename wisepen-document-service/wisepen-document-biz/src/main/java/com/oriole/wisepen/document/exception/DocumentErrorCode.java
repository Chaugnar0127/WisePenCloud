package com.oriole.wisepen.document.exception;

import com.oriole.wisepen.common.core.exception.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档模块错误码枚举
 * 范围：2000-2999
 */
@Getter
@AllArgsConstructor
public enum DocumentErrorCode implements IErrorCode {

    DOCUMENT_OPERATION_ERROR(50000, "文档操作错误"),
    DOCUMENT_NOT_FOUND(50001, "文档不存在"),
    DOCUMENT_PERMISSION_DENIED(50002, "对不起，您没有该资源的访问/操作权限"),
    DOCUMENT_NOT_READY(50003, "文档尚未就绪，请稍后再试"),
    DOCUMENT_DOWNLOAD_ERROR(50004, "文档下载失败"),
    DOCUMENT_PREVIEW_ERROR(50005, "文档预览失败"),


    DOCUMENT_TYPE_NOT_ALLOWED(20011, "不支持的文件类型，仅支持 doc/docx/ppt/pptx/xls/xlsx/pdf"),
    DOCUMENT_UPLOAD_ERROR(50012, "文档上传失败"),
    DOCUMENT_PROCESS_CANCEL_ERROR(50013, "文档处理取消错误"),
    DOCUMENT_ALREADY_READY(50014, "文档已就绪，不可在此取消，请在资源库中进行删除"),
    DOCUMENT_CONVERTING_AND_PARSING(50014, "文档正在转换与解析，不可在此取消，请稍后"),
    DOCUMENT_RETRY_STATE_INVALID(50015, "仅能重试状态为 FAILED/REGISTERING_RES_TIMEOUT 的文档"),
    DOCUMENT_REGISTER_RESOURCE_ERROR(50016, "文档资源注册失败"),

    DOCUMENT_CONVERT_ERROR(50021, "文档转换失败"),
    DOCUMENT_READ_ERROR(50022, "文档读取失败");

    private final Integer code;
    private final String msg;
}
