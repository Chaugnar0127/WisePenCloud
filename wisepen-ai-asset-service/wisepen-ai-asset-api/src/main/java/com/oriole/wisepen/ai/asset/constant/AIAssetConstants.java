package com.oriole.wisepen.ai.asset.constant;

import com.oriole.wisepen.resource.enums.ResourceType;

import java.util.Set;

public interface AIAssetConstants {
    public static final Set<ResourceType> ALLOWED_TYPES = Set.of(
            ResourceType.SKILL,
            ResourceType.AGENT
    );
}
