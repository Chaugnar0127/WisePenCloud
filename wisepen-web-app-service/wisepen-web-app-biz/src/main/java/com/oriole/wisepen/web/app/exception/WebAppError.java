package com.oriole.wisepen.web.app.exception;

import com.oriole.wisepen.common.core.domain.IResult;
import com.oriole.wisepen.common.core.domain.ResultKey;
import com.oriole.wisepen.common.core.domain.enums.BusinessDomain;
import com.oriole.wisepen.common.core.exception.ErrorReason;
import com.oriole.wisepen.web.app.constant.WebAppSubject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebAppError implements IResult {
    SINGLE_HTML_PAGE_NOT_FOUND(10111, new ResultKey(BusinessDomain.WEB_APP, WebAppSubject.SINGLE_HTML_PAGE, ErrorReason.NOT_FOUND), "Single HTML Page 不存在"),
    SINGLE_HTML_PAGE_PERMISSION_DENIED(10121, new ResultKey(BusinessDomain.WEB_APP, WebAppSubject.SINGLE_HTML_PAGE, ErrorReason.PERMISSION_DENIED), "无权访问或操作该 Single HTML Page"),
    SINGLE_HTML_PAGE_CONTENT_TOO_LARGE(10131, new ResultKey(BusinessDomain.WEB_APP, WebAppSubject.SINGLE_HTML_PAGE, ErrorReason.INVALID), "Single HTML Page 内容超过大小限制"),
    SINGLE_HTML_PAGE_CONTENT_READ_FAILED(10141, new ResultKey(BusinessDomain.WEB_APP, WebAppSubject.SINGLE_HTML_PAGE, ErrorReason.FAILED), "读取 Single HTML Page 内容失败"),

    SINGLE_HTML_PAGE_VERSION_NOT_FOUND(10211, new ResultKey(BusinessDomain.WEB_APP, WebAppSubject.SINGLE_HTML_PAGE_VERSION, ErrorReason.NOT_FOUND), "Single HTML Page 版本不存在");

    private final Integer code;
    private final ResultKey key;
    private final String msg;
}
