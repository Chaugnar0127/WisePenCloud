package com.oriole.wisepen.document.consumer;

import com.oriole.wisepen.document.api.constant.DocumentConstants;
import com.oriole.wisepen.document.service.IDocumentService;
import com.oriole.wisepen.resource.domain.mq.ResourceForkMessage;
import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.oriole.wisepen.resource.constant.MqTopicConstants.TOPIC_RESOURCE_FORK;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceForkConsumer {

    private final IDocumentService documentService;

    @KafkaListener(topics = TOPIC_RESOURCE_FORK, groupId = "wisepen-document-fork-group")
    public void onResourceFork(ResourceForkMessage message) {
        ResourceType type = message.getResourceType();
        if (!DocumentConstants.ALLOWED_TYPES.contains(type)) {
            return;
        }
        log.info("resourceFork received topic={} sourceResourceId={} newResourceId={} resourceType={}",
                TOPIC_RESOURCE_FORK, message.getSourceResourceId(), message.getNewResourceId(), type);
        documentService.forkDocument(message);
    }
}

