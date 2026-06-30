package com.oriole.wisepen.document.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "document_content")
public class DocumentContentEntity implements Persistable<String> {
    @Id
    private String documentId;

    private Integer version;

    private String rawText;

    private String markdown;

    @CreatedDate
    private LocalDateTime createTime;

    @Override
    public String getId() {
        return documentId;
    }

    @Override
    @Transient
    public boolean isNew() {
        return createTime == null;
    }
}
