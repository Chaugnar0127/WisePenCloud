package com.oriole.wisepen.skill.constant;

import com.oriole.wisepen.common.core.domain.IBusinessSubject;

import java.util.Locale;

public enum SkillSubject implements IBusinessSubject {
    SKILL;

    @Override
    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}