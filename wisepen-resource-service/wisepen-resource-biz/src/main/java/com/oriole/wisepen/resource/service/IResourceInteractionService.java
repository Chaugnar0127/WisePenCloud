package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.resource.domain.dto.req.ResourceRateRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceLikeRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceReadRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceUserInteractionRecordResponse;

import java.util.Map;

public interface IResourceInteractionService {

    ResourceUserInteractionRecordResponse getResourceUserInteractionInfo(String resourceId, String userId);

    void changeResourceReadStatus(ResourceReadRequest request, String userId, Map<Long, GroupRoleType> groupRoles);

    void changeResourceLikeStatus(ResourceLikeRequest request, String userId, Map<Long, GroupRoleType> groupRoles);

    void changeResourceScore(ResourceRateRequest request, String userId);
}
