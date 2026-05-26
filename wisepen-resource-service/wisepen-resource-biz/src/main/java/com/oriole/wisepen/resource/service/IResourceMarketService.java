package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.resource.domain.dto.req.ResourcePublishSellRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceReviewSellRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceMarketDetailResponse;

import java.util.Map;

public interface IResourceMarketService {

    String publishSellInfo(ResourcePublishSellRequest req, Long userId);

    void reviewSellInfo(ResourceReviewSellRequest req, Long reviewerId, IdentityType identityType,
            Map<Long, GroupRoleType> groupRoles);

    ResourceMarketDetailResponse getMarketDetail(String resourceId, String groupId);
}
