package com.oriole.wisepen.media.domain.entity;

import com.oriole.wisepen.media.api.domain.base.MediaStatus;
import com.oriole.wisepen.media.api.domain.base.MediaUploadMeta;
import com.oriole.wisepen.media.api.enums.ForensicCapability;
import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wisepen_media_info")
@CompoundIndex(
        name = "idx_resource_id",
        def = "{'resourceId': 1}",
        unique = true,
        partialFilter = "{'resourceId': {'$exists': true, '$ne': null}}"
)
public class MediaInfoEntity implements Persistable<String> {

    @Id
    private String mediaId;

    private String resourceId;

    private Long ownerId;

    private ResourceType resourceType;

    private String sourceObjectKey;

    private String sourceHlsPrefix;

    private List<String> sourceHlsObjectKeys;

    private String previewObjectKey;

    private String posterObjectKey;

    private Long durationMs;

    private Integer width;

    private Integer height;

    private Long size;

    private MediaStatus mediaStatus;

    private ForensicCapability forensicCapability;

    private MediaUploadMeta uploadMeta;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @Override
    public String getId() {
        return mediaId;
    }

    @Override
    @Transient
    public boolean isNew() {
        return createTime == null;
    }
}
