package com.oriole.wisepen.document.exception;

import com.oriole.wisepen.common.core.exception.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档模块错误码枚举
 * 范围：2000-2999
 *
 * @author Ian.xiong
 */
@Getter
@AllArgsConstructor
public enum DocumentErrorCode implements IErrorCode {

    DOCUMENT_UPLOAD_ERROR(2001, "文档上传失败"),
    DOCUMENT_CONVERT_ERROR(2002, "文档转换失败"),
    DOCUMENT_NOT_FOUND(2003, "文档不存在"),
    DOCUMENT_READ_ERROR(2004, "文档读取失败"),
    DOCUMENT_TYPE_NOT_SUPPORTED(2005, "不支持的文件类型"),
    DOCUMENT_TYPE_NOT_ALLOWED(2006, "不支持的文件类型，仅支持 doc/docx/ppt/pptx/xls/xlsx/pdf"),
    DOCUMENT_MD5_MISMATCH(2007, "文件校验失败"),
    DOCUMENT_MAGIC_NUMBER_MISMATCH(2008, "文件类型与扩展名不匹配"),
    DOCUMENT_DELETE_ERROR(2009, "文档删除失败"),
    DOCUMENT_OPERATION_FORBIDDEN(2010, "当前状态不允许此操作"),
    DOCUMENT_SIZE_EXCEEDED(2011, "文件大小超过100MB限制"),
    DOCUMENT_RENAME_ERROR(2012, "文档重命名失败"),
    DOCUMENT_PERMISSION_DENIED(2013, "无权访问此文档"),
    DOCUMENT_DOWNLOAD_ERROR(2014, "文档下载失败"),
    DOCUMENT_NOT_READY(2015, "文档尚未就绪，请稍后再试");

    private final Integer code;
    private final String msg;
}
