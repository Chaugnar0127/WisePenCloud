package com.oriole.wisepen.resource.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResourceLifecycleStatus {
    READY,
    FORKING,
    FORK_FAILED
}
