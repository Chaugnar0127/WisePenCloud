package com.oriole.wisepen.ai.asset.constant;

import com.oriole.wisepen.common.core.domain.IBusinessSubject;

import java.util.Locale;

public enum AIAssetSubject implements IBusinessSubject {
    SKILL;

    @Override
    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}