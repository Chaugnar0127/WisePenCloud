package com.oriole.wisepen.note.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.note.api.domain.mq.NoteSnapshotMessage;
import com.oriole.wisepen.note.domain.enums.VersionType;
import com.oriole.wisepen.note.service.INoteService;
import com.oriole.wisepen.note.service.INoteVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Base64;

import static com.oriole.wisepen.note.api.constant.MqTopicConstants.TOPIC_NOTE_SNAPSHOT;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteSnapshotConsumer {

    private final INoteService noteService;
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
            process(msg);
        } catch (Exception e) {
            log.error("快照消费处理失败, resourceId={}, version={}", msg.getResourceId(), msg.getVersion(), e);
        }
    }

    private void process(NoteSnapshotMessage msg) {
        byte[] snapshotData = Base64.getDecoder().decode(msg.getData());
        String updatedBy = msg.getUpdatedBy() != null
                ? String.join(",", msg.getUpdatedBy()) : "system";

        // 1. FULL 消息：更新 note_documents 中的最新快照（含纯文本）
        if ("FULL".equals(msg.getType())) {
            noteService.upsertSnapshot(msg.getResourceId(), snapshotData,
                    msg.getVersion(), updatedBy, msg.getPlainText());
        }

        // 2. 写入版本历史（FULL 和 DELTA 都存）
        VersionType type = VersionType.valueOf(msg.getType());
        noteVersionService.createVersion(
                msg.getResourceId(), msg.getVersion(), type,
                snapshotData, null, updatedBy);

        log.info("快照消费完成: resourceId={}, version={}, type={}",
                msg.getResourceId(), msg.getVersion(), msg.getType());
    }
}
