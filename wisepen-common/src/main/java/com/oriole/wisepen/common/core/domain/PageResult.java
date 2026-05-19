package com.oriole.wisepen.common.core.domain;

/**
 * 兼容已有分页返回命名。
 * 当前实现直接复用 PageR，避免各服务重复维护两套分页结构。
 */
public class PageResult<T> extends PageR<T> {

    public PageResult() {
        super();
    }

    public PageResult(long total, int page, int size) {
        super(total, page, size);
    }
}
