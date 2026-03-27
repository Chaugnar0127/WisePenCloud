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
    NOTE_PERMISSION_DENIED(60002, "对不起，您没有该笔记的访问权限"),
    NOTE_VERSION_NOT_FOUND(60003, "指定的版本不存在"),
    NOTE_SNAPSHOT_EMPTY(60004, "笔记快照为空，无法恢复"),
    NOTE_REVERT_FAILED(60005, "版本回退失败"),
    NOTE_CHECKPOINT_MISSING(60006, "未找到有效的检查点，无法恢复版本");

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
