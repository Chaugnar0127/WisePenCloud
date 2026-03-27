package com.oriole.wisepen.note.domain.entity;

import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "note_operation_logs")
@CompoundIndex(name = "idx_resource_timestamp", def = "{'resourceId': 1, 'timestamp': -1}")
public class NoteOperationLogEntity {
    @Id
    private String id;

    private String resourceId;
    private String userId;

    /** INSERT / DELETE / PASTE / FORMAT / UNDO / REDO 等 */
    private String operationType;

    /** Yjs update 二进制（可选） */
    private Binary updateData;

    private String contentSummary;
    private Date timestamp;

    /** 合并的原子操作数（颗粒度合并时 >1） */
    private Integer mergedCount;

    /** BlockNote 树状突变详情 */
    private Object details;
}
