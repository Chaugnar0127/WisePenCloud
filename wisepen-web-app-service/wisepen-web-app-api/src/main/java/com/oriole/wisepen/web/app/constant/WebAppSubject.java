package com.oriole.wisepen.web.app.constant;

import com.oriole.wisepen.common.core.domain.IBusinessSubject;

import java.util.Locale;

public enum WebAppSubject implements IBusinessSubject {
    SINGLE_HTML_PAGE,
    SINGLE_HTML_PAGE_VERSION;

    @Override
    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}
