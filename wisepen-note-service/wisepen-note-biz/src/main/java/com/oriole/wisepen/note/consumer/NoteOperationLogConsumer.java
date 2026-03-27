package com.oriole.wisepen.note.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.note.api.domain.mq.NoteOperationLogMessage;
import com.oriole.wisepen.note.service.INoteOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.oriole.wisepen.note.api.constant.MqTopicConstants.TOPIC_NOTE_OPLOG;
import static com.oriole.wisepen.note.api.constant.MqTopicConstants.TOPIC_NOTE_SNAPSHOT;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteOperationLogConsumer {

    private final INoteOperationLogService noteOperationLogService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = TOPIC_NOTE_OPLOG,
            groupId = "wisepen-note-oplog-group",
            properties = {
                    "value.deserializer=org.apache.kafka.common.serialization.StringDeserializer"
            }
    )
    public void onOperationLog(String payload) {
        NoteOperationLogMessage msg;
        try {
            msg = objectMapper.readValue(payload, NoteOperationLogMessage.class);
        } catch (Exception e) {
            log.error("NoteOperationLogMessage 反序列化失败, payload={}", payload, e);
            return;
        }

        try {
            noteOperationLogService.batchSave(msg);
            log.debug("操作日志消费完成: resourceId={}, count={}",
                    msg.getResourceId(), msg.getEntries().size());
        } catch (Exception e) {
            log.error("操作日志消费处理失败, resourceId={}", msg.getResourceId(), e);
        }
    }
}
