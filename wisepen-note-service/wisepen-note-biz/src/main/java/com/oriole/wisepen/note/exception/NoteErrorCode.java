package com.oriole.wisepen.note.exception;

import com.oriole.wisepen.common.core.exception.IErrorCode;
import lombok.AllArgsConstructor;

/**
 * 笔记微服务专属业务错误码
 * 号段：60000 - 69999
 */
@AllArgsConstructor
public enum NoteErrorCode implements IErrorCode {

    NOTE_NOT_FOUND(60001, "笔记不存在"),
    NOTE_PERMISSION_DENIED(60002, "对不起，您没有该资源的访问/操作权限");

    private final Integer code;
    private final String msg;

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
