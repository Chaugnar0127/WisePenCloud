package com.oriole.wisepen.document.domain.entity;

import com.oriole.wisepen.document.api.enums.DocumentEditSessionStatus;
import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wisepen_document_edit_sessions")
public class DocumentEditSessionEntity {

    @Id
    private String sessionId;

    @Indexed
    private String resourceId;

    private String baseDocumentId;

    private Integer baseVersion;

    @Indexed(unique = true)
    private String documentKey;

    private ResourceType fileType;

    private String documentName;

    private String sourceObjectKey;

    private Long initiatorUserId;

    private DocumentEditSessionStatus status;

    private String latestDraftObjectKey;

    private String errorMessage;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    private LocalDateTime expiresAt;
}
