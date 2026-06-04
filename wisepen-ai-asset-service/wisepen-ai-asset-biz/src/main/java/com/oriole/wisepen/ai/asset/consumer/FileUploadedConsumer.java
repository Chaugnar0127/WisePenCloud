package com.oriole.wisepen.ai.asset.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.ai.asset.service.ISkillVersionService;
import com.oriole.wisepen.file.storage.api.domain.mq.FileUploadedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.oriole.wisepen.file.storage.api.constant.MqTopicConstants.TOPIC_FILE_UPLOADED;

@Component
@RequiredArgsConstructor
public class FileUploadedConsumer {

    private final ISkillVersionService skillVersionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC_FILE_UPLOADED, groupId = "wisepen-skill-upload-callback-group")
    public void onFileUploaded(String payload) throws JsonProcessingException {
        // 从兼容非Java微服务的发布者订阅，使用objectMapper显式转换
        FileUploadedMessage message = objectMapper.readValue(payload, FileUploadedMessage.class);
        skillVersionService.handleFileUploaded(message);
    }
}