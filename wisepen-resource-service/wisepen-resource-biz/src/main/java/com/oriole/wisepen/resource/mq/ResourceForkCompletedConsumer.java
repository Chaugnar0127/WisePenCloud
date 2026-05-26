package com.oriole.wisepen.resource.mq;

import com.oriole.wisepen.resource.domain.mq.ResourceForkCompletedMessage;
import com.oriole.wisepen.resource.service.IResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.oriole.wisepen.resource.constant.MqTopicConstants.TOPIC_RESOURCE_FORK_COMPLETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceForkCompletedConsumer {

    private final IResourceService resourceService;

    @KafkaListener(topics = TOPIC_RESOURCE_FORK_COMPLETED, groupId = "wisepen-resource-fork-completed-group")
    public void onForkCompleted(ResourceForkCompletedMessage message) {
        log.info("resourceForkCompleted received topic={} newResourceId={} success={} resourceType={}",
                TOPIC_RESOURCE_FORK_COMPLETED, message.getNewResourceId(), message.isSuccess(), message.getResourceType());
        try {
            resourceService.onForkCompleted(message);
        } catch (Exception e) {
            log.error("resourceForkCompleted consume failed topic={} newResourceId={}",
                    TOPIC_RESOURCE_FORK_COMPLETED, message.getNewResourceId(), e);
            throw e;
        }
    }
}
