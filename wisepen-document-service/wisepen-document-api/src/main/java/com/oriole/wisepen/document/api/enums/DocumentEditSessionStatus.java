package com.oriole.wisepen.document.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentEditSessionStatus {
    ACTIVE("ACTIVE"),
    DRAFT_SAVED("DRAFT_SAVED"),
    SAVING("SAVING"),
    FINISHED("FINISHED"),
    FAILED("FAILED");

    private final String value;
}
