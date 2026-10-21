package com.oriole.wisepen.web.app.domain.entity;

import com.oriole.wisepen.web.app.domain.base.SingleHtmlPageVersionBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wisepen_single_html_page_versions")
@CompoundIndex(
        name = "idx_resource_version",
        def = "{'resourceId': 1, 'version': 1}",
        unique = true
)
public class SingleHtmlPageVersionEntity extends SingleHtmlPageVersionBase {
    @Id
    private String singleHtmlPageId;

    private String resourceId;
    private Integer version;
    private String objectKey;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;
}
