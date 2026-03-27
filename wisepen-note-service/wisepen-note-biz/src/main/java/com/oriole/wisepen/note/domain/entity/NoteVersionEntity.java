package com.oriole.wisepen.note.domain.entity;

import com.oriole.wisepen.note.domain.enums.VersionType;
import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "note_versions")
@CompoundIndex(name = "idx_resource_version", def = "{'resourceId': 1, 'version': 1}", unique = true)
public class NoteVersionEntity {
    @Id
    private String id;

    private String resourceId;
    private Long version;

    /** FULL（检查点全量）或 DELTA（增量） */
    private VersionType type;

    /** FULL 时为完整 Yjs 快照; DELTA 时为增量 Update */
    private Binary data;

    private String label;
    private Date createdAt;
    private String createdBy;
}
