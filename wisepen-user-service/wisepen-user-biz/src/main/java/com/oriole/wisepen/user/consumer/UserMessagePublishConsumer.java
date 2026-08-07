package com.oriole.wisepen.user.consumer;

import com.oriole.wisepen.user.api.domain.dto.req.MessagePublishRequest;
import com.oriole.wisepen.user.service.IMessageService;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.plugins.kafka.asyncapi.annotations.KafkaAsyncOperationBinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.oriole.wisepen.user.api.constant.MqTopicConstants.TOPIC_USER_MESSAGE_PUBLISH;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMessagePublishConsumer {

    private final IMessageService messageService;

    @KafkaListener(topics = TOPIC_USER_MESSAGE_PUBLISH, groupId = "wisepen-user-message-publish-group",
            properties = {
                    "spring.json.use.type.headers:false",
                    "spring.json.value.default.type:com.oriole.wisepen.user.api.domain.dto.req.MessagePublishRequest"
            }
    )
    @AsyncListener(operation = @AsyncOperation(
            channelName = TOPIC_USER_MESSAGE_PUBLISH,
            description = "消费通用站内信发布请求，按消息服务的幂等与重复拦截规则写入收件箱。",
            payloadType = MessagePublishRequest.class,
            message = @AsyncMessage(name = "MessagePublishRequest", title = "站内信发布请求")
    ))
    @KafkaAsyncOperationBinding(groupId = "wisepen-user-message-publish-group")
    public void onMessagePublish(MessagePublishRequest request) {
        log.info("user message publish event received. topic={} bizTraceId={}",
                TOPIC_USER_MESSAGE_PUBLISH, request.getBizTraceId());
        try {
            messageService.publishMessage(request);
            log.debug("user message publish event consumed. topic={} bizTraceId={}",
                    TOPIC_USER_MESSAGE_PUBLISH, request.getBizTraceId());
        } catch (Exception e) {
            log.error("user message publish event consumption failed. topic={} bizTraceId={}",
                    TOPIC_USER_MESSAGE_PUBLISH, request.getBizTraceId(), e);
            throw e;
        }
    }
}
