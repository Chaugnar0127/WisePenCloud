package com.oriole.wisepen.note.domain.entity;

import com.oriole.wisepen.note.api.domain.base.NoteVersionBase;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@SuperBuilder
@Document(collection = "note_versions")
@CompoundIndex(name = "idx_resource_version", def = "{'resourceId': 1, 'version': 1}", unique = true)
public class NoteVersionEntity extends NoteVersionBase {
    @Id
    private String id;
    private String resourceId;
    /** FULL 时为完整 Yjs 快照; DELTA 时为增量 Update */
    private Binary data;
}