package com.oriole.wisepen.media.domain.entity;

import com.oriole.wisepen.media.api.enums.MediaDownloadJobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wisepen_media_download_jobs")
public class MediaDownloadJobEntity implements Persistable<String> {

    @Id
    private String jobId;

    private String sessionId;

    private String resourceId;

    private String mediaId;

    private Long requesterId;

    private MediaDownloadJobStatus status;

    private String outputObjectKey;

    private String failReason;

    private LocalDateTime expiresAt;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @Override
    public String getId() {
        return jobId;
    }

    @Override
    @Transient
    public boolean isNew() {
        return createTime == null;
    }
}
