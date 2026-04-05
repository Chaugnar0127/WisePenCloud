package com.oriole.wisepen.resource.mq;

import com.oriole.wisepen.resource.constant.MqTopicConstants;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.resource.domain.mq.AclRecalculateMessage;
import com.oriole.wisepen.resource.domain.mq.ResourceDeletedMessage;
import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.oriole.wisepen.resource.constant.MqTopicConstants.TOPIC_ACL_RECALC;

@Slf4j
@Component // 或者 @Service
@RequiredArgsConstructor
public class KafkaEventPublisherImpl implements IEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishAclRecalculateEvent(String resourceId, String triggerSource) {
        try {
            AclRecalculateMessage msg = AclRecalculateMessage.builder().resourceId(resourceId).triggerSource(triggerSource).build();
            kafkaTemplate.send(TOPIC_ACL_RECALC, resourceId, msg);
            log.debug("成功发布 ACL 重算事件, ResourceId: {}", resourceId);
        } catch (Exception e) {
            log.error("发布 ACL 重算事件失败, ResourceId: {}", resourceId, e);
        }
    }

    @Override
    public void publishResDeletedEvent(List<ResourceItemEntity> resourceList) {
        if (resourceList == null || resourceList.isEmpty()) {
            return;
        }
        List<String> resourceIds = resourceList.stream()
                .map(ResourceItemEntity::getResourceId).collect(Collectors.toList());
        try {
            Map<ResourceType, List<String>> typedResourceIds = resourceList.stream()
                    .collect(Collectors.groupingBy(
                            entity -> entity.getResourceType() != null ? entity.getResourceType() : ResourceType.UNKNOWN,
                            Collectors.mapping(ResourceItemEntity::getResourceId, Collectors.toList())
                    ));
            ResourceDeletedMessage message = ResourceDeletedMessage.builder().typedResourceIds(typedResourceIds).build();
            kafkaTemplate.send(MqTopicConstants.TOPIC_RESOURCE_PHYSICAL_DESTROY, message);
            log.debug("成功发布资源删除事件, ResourceId: {}", resourceIds);
        } catch (Exception e) {
            log.error("发布资源删除事件失败, ResourceId: {}", resourceIds, e);
        }
    }
}