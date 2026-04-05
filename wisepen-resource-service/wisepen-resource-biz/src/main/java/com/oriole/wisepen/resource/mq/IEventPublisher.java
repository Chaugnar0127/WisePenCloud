package com.oriole.wisepen.resource.mq;

import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;

import java.util.List;

public interface IEventPublisher {
    void publishAclRecalculateEvent(String resourceId, String triggerSource);
    void publishResDeletedEvent(List<ResourceItemEntity> resourceList);
}
