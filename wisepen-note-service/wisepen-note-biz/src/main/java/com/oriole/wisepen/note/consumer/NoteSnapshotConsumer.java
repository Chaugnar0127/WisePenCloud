package com.oriole.wisepen.note.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.note.api.domain.mq.NoteSnapshotMessage;
import com.oriole.wisepen.note.service.INoteVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.oriole.wisepen.note.api.constant.MqTopicConstants.TOPIC_NOTE_SNAPSHOT;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteSnapshotConsumer {

    private final INoteVersionService noteVersionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = TOPIC_NOTE_SNAPSHOT,
            groupId = "wisepen-note-snapshot-group",
            properties = {
                    "value.deserializer=org.apache.kafka.common.serialization.StringDeserializer"
            }
    )
    public void onSnapshot(String payload) {
        NoteSnapshotMessage msg;
        try {
            msg = objectMapper.readValue(payload, NoteSnapshotMessage.class);
        } catch (Exception e) {
            log.error("NoteSnapshotMessage 反序列化失败, payload={}", payload, e);
            return;
        }

        try {
            noteVersionService.createVersion(msg);
            log.info("快照消费完成: resourceId={}, version={}, type={}", msg.getResourceId(), msg.getVersion(), msg.getType());
        } catch (Exception e) {
            log.error("快照消费处理失败, resourceId={}, version={}", msg.getResourceId(), msg.getVersion(), e);
        }
    }
}
