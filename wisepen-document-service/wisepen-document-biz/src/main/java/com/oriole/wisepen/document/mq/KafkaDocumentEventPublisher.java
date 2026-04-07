package com.oriole.wisepen.document.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.document.api.domain.mq.DocumentParseTaskMessage;
import com.oriole.wisepen.document.api.domain.mq.DocumentReadyMessage;
import com.oriole.wisepen.file.storage.api.domain.mq.FileUploadedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.oriole.wisepen.document.api.constant.MqTopicConstants.TOPIC_DOCUMENT_PARSE;
import static com.oriole.wisepen.document.api.constant.MqTopicConstants.TOPIC_DOCUMENT_READY;
import static com.oriole.wisepen.file.storage.api.constant.MqTopicConstants.TOPIC_FILE_DELETE;

/**
 * 文档服务 Kafka 事件发布器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDocumentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 发布文档解析任务（内部削峰）
    public void publishParseTask(DocumentParseTaskMessage msg) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(msg);
            kafkaTemplate.send(TOPIC_DOCUMENT_PARSE, msg.getDocumentId(), jsonMessage);
            log.debug("成功发布文档解析事件 Document: {}", msg.getDocumentId());
        } catch (Exception e) {
            log.error("发布文档解析事件失败 Document: {}", msg.getDocumentId(), e);
        }
    }

    // 发布文档处理就绪事件
    public void publishReadyEvent(DocumentReadyMessage msg) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(msg);
            kafkaTemplate.send(TOPIC_DOCUMENT_READY, msg.getResourceId(), jsonMessage);
            log.debug("成功发布文档就绪事件 Document: {}", msg.getResourceId());
        } catch (Exception e) {
            log.error("发布文档就绪事件失败 Document: {}", msg.getResourceId(), e);
        }
    }

    // 发布文件删除事件
    public void publishFileDeleteEvent(List<String> allObjectKeys) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(allObjectKeys);
            kafkaTemplate.send(TOPIC_FILE_DELETE, jsonPayload);
            log.debug("成功发布文档删除事件 Document: {}", allObjectKeys);
        } catch (Exception e) {
            log.error("发布文档解析删除失败 Document: {}", allObjectKeys, e);
        }
    }
}