package com.oriole.wisepen.document.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "document_content")
public class DocumentContentEntity {

    @Id
    private String id;

    @Field("document_id")
    private String documentId;

    @Field("raw_text")
    private String rawText;

    @Field("create_time")
    private LocalDateTime createTime;
}
