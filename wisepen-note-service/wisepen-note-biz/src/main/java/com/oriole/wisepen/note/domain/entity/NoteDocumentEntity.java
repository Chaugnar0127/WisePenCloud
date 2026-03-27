package com.oriole.wisepen.note.domain.entity;

import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "note_documents")
public class NoteDocumentEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    private String resourceId;

    /** Y.encodeStateAsUpdate(yDoc) 完整二进制 */
    private Binary fullSnapshot;

    private Long version;
    private Date lastUpdatedAt;
    private String lastUpdatedBy;

    /** FULL 检查点时 Node 提取的纯文本，用于全文检索 */
    private String plainText;
}
