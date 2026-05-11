package com.oriole.wisepen.resource.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OwnershipTier {
    WATERMARK(1,"水印版",11),
    ORIGINAL(2,"原版",27);

    private final int code;
    private final String desc;
    private final int permissionMask;
}
