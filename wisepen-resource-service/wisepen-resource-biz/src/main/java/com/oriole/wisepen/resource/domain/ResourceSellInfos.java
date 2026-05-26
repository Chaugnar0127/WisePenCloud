package com.oriole.wisepen.resource.domain;

import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.resource.exception.ResourceError;

import java.util.Objects;

public final class ResourceSellInfos {

    private ResourceSellInfos() {
    }

    public static ResourceSellInfo requireSellInfo(ResourceItemEntity resource, String sellId) {
        return resource.getSellInfos().stream()
                .filter(sellInfo -> Objects.equals(sellInfo.getSellId(), sellId))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ResourceError.SELL_INFO_NOT_FOUND));
    }
}
