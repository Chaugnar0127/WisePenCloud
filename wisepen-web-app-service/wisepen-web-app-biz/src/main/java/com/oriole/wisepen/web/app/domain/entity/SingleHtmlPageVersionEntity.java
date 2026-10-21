package com.oriole.wisepen.web.app.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wisepen_single_html_page_versions")
@CompoundIndex(name = "idx_resource_version", def = "{'resourceId': 1, 'version': 1}", unique = true)
public class SingleHtmlPageVersionEntity {
    @Id
    private String id;

    private String resourceId;
    private Integer version;
    private String objectKey;
    private Long size;
    private String contentHash;
    private Long creatorUserId;

    @CreatedDate
    private LocalDateTime createTime;
}
