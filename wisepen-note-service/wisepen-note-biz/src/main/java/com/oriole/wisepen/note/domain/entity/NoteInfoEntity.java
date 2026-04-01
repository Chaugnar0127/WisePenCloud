package com.oriole.wisepen.note.domain.entity;

import com.oriole.wisepen.note.api.domain.base.NoteInfoBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "note_documents")
public class NoteInfoEntity extends NoteInfoBase {
    @Id
    private String resourceId;

    /** FULL 检查点时 Node 提取的纯文本，用于全文检索 */
    private String plainText;
}