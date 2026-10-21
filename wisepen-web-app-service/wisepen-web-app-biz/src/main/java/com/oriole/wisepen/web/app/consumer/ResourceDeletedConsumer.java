package com.oriole.wisepen.web.app.consumer;

import com.oriole.wisepen.resource.domain.mq.ResourceDeletedMessage;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.web.app.service.ISingleHtmlPageService;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.plugins.kafka.asyncapi.annotations.KafkaAsyncOperationBinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.oriole.wisepen.common.core.util.LogIdUtils.summarizeIds;
import static com.oriole.wisepen.resource.constant.MqTopicConstants.TOPIC_RESOURCE_PHYSICAL_DESTROY;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceDeletedConsumer {

    private final ISingleHtmlPageService singleHtmlPageService;

    @KafkaListener(topics = TOPIC_RESOURCE_PHYSICAL_DESTROY, groupId = "wisepen-web-app-resource-destroy-group")
    @AsyncListener(operation = @AsyncOperation(
            channelName = TOPIC_RESOURCE_PHYSICAL_DESTROY,
            description = "消费资源物理删除事件，清理 Single HTML Page 主档和全部历史版本。",
            payloadType = ResourceDeletedMessage.class,
            message = @AsyncMessage(name = "ResourceDeletedMessage", title = "资源物理删除事件")
    ))
    @KafkaAsyncOperationBinding(groupId = "wisepen-web-app-resource-destroy-group")
    public void onResourceDeleted(ResourceDeletedMessage message) {
        Map<ResourceType, List<String>> typedResourceIds = message.getTypedResourceIds();
        List<String> resourceIds = typedResourceIds == null
                ? null : typedResourceIds.get(ResourceType.SINGLE_HTML_PAGE);
        int count = resourceIds == null ? 0 : resourceIds.size();
        log.info("single HTML page resource delete event received. topic={} count={} resourceIds={}",
                TOPIC_RESOURCE_PHYSICAL_DESTROY, count, summarizeIds(resourceIds));
        if (count == 0) {
            log.debug("single HTML page resource delete event skipped because no page resources. topic={}",
                    TOPIC_RESOURCE_PHYSICAL_DESTROY);
            return;
        }
        try {
            singleHtmlPageService.deletePages(resourceIds);
            log.debug("single HTML page resource delete event consumed. topic={} count={} resourceIds={}",
                    TOPIC_RESOURCE_PHYSICAL_DESTROY, count, summarizeIds(resourceIds));
        } catch (Exception e) {
            log.error("single HTML page resource delete event consumption failed. topic={} count={} resourceIds={}",
                    TOPIC_RESOURCE_PHYSICAL_DESTROY, count, summarizeIds(resourceIds), e);
            throw e;
        }
    }
}
