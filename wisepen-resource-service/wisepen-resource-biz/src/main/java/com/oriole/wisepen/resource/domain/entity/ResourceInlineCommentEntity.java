package com.oriole.wisepen.resource.domain.entity;

import com.oriole.wisepen.resource.domain.base.ResourceInlineCommentBase;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wisepen_resource_inline_comments")
@CompoundIndexes({
        @CompoundIndex(def = "{'resourceId': 1, 'deletedAt': 1, 'resolved': 1, 'updateTime': -1}"),
        @CompoundIndex(def = "{'resourceId': 1, 'applicableFromVersion': 1, 'applicableToVersion': 1}"),
        @CompoundIndex(def = "{'resourceId': 1, 'anchorRef.externalAnchorId': 1}")
})
public class ResourceInlineCommentEntity extends ResourceInlineCommentBase {
    @Id
    private String inlineCommentId; // 行内 CommentId

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

}
