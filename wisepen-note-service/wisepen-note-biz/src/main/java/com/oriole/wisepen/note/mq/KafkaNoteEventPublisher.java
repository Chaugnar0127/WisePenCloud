package com.oriole.wisepen.note.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.resource.domain.mq.ResourceForkCompletedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.oriole.wisepen.resource.constant.MqTopicConstants.TOPIC_RESOURCE_FORK_COMPLETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaNoteEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishForkCompleted(ResourceForkCompletedMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(TOPIC_RESOURCE_FORK_COMPLETED, message.getNewResourceId(), jsonMessage);
            log.info("发送 Note Fork 回执 NewResourceId={} | Success={}",
                    message.getNewResourceId(), message.isSuccess());
        } catch (Exception e) {
            log.error("发送 Note Fork 回执失败 NewResourceId={}", message.getNewResourceId(), e);
        }
    }
}

