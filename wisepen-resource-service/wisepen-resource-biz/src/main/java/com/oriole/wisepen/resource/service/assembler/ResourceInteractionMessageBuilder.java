package com.oriole.wisepen.resource.service.assembler;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.oriole.wisepen.common.core.domain.enums.BusinessDomain;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.user.api.constant.MessageTemplatePlaceholders;
import com.oriole.wisepen.user.api.domain.dto.req.MessagePublishRequest;
import com.oriole.wisepen.user.api.enums.MessageDeliveryScope;
import com.oriole.wisepen.user.api.enums.MessageType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResourceInteractionMessageBuilder {

    public static MessagePublishRequest like(ResourceItemEntity resource, String actorUserId) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("actionType", "LIKE");
        extra.put("resourceId", resource.getResourceId());
        extra.put("actorUserId", Long.valueOf(actorUserId));
        return MessagePublishRequest.builder()
                .receiverUserIds(List.of(Long.valueOf(resource.getOwnerId())))
                .deliveryScope(MessageDeliveryScope.DIRECT)
                .messageType(MessageType.RESOURCE_INTERACTION)
                .title("资源被点赞")
                .content(MessageTemplatePlaceholders.user(actorUserId) + " 点赞了你的资源 " + resource.getResourceName())
                .jumpUrl(null)
                .sourceService(BusinessDomain.RESOURCE)
                .bizTraceId("RESOURCE_INTERACTION:LIKE:" + resource.getResourceId() + ":" + actorUserId + ":" + IdUtil.fastSimpleUUID())
                .extra(JSONUtil.toJsonStr(extra))
                .build();
    }

    public static MessagePublishRequest comment(ResourceItemEntity resource, String actorUserId,
                                                String commentId, String content) {
        String commentContent = content;
        if (commentContent.length() > 80) {
            commentContent = commentContent.substring(0, 80);
        }
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("actionType", "COMMENT");
        extra.put("resourceId", resource.getResourceId());
        extra.put("actorUserId", Long.valueOf(actorUserId));
        extra.put("commentId", commentId);
        return MessagePublishRequest.builder()
                .receiverUserIds(List.of(Long.valueOf(resource.getOwnerId())))
                .deliveryScope(MessageDeliveryScope.DIRECT)
                .messageType(MessageType.RESOURCE_INTERACTION)
                .title("资源收到新评论")
                .content(MessageTemplatePlaceholders.user(actorUserId) + " 评论了你的资源 " + resource.getResourceName() + "：" + commentContent)
                .jumpUrl(null)
                .sourceService(BusinessDomain.RESOURCE)
                .bizTraceId("RESOURCE_INTERACTION:COMMENT:" + commentId)
                .extra(JSONUtil.toJsonStr(extra))
                .build();
    }

    public static MessagePublishRequest favorite(ResourceItemEntity resource, String actorUserId) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("actionType", "FAVORITE");
        extra.put("resourceId", resource.getResourceId());
        extra.put("actorUserId", Long.valueOf(actorUserId));
        return MessagePublishRequest.builder()
                .receiverUserIds(List.of(Long.valueOf(resource.getOwnerId())))
                .deliveryScope(MessageDeliveryScope.DIRECT)
                .messageType(MessageType.RESOURCE_INTERACTION)
                .title("资源被收藏")
                .content(MessageTemplatePlaceholders.user(actorUserId) + " 收藏了你的资源 " + resource.getResourceName())
                .jumpUrl(null)
                .sourceService(BusinessDomain.RESOURCE)
                .bizTraceId("RESOURCE_INTERACTION:FAVORITE:" + resource.getResourceId() + ":" + actorUserId + ":" + IdUtil.fastSimpleUUID())
                .extra(JSONUtil.toJsonStr(extra))
                .build();
    }

    public static MessagePublishRequest commentLike(ResourceItemEntity resource, String actorUserId,
                                                    String commentId, String commentAuthorId) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("actionType", "COMMENT_LIKE");
        extra.put("resourceId", resource.getResourceId());
        extra.put("commentId", commentId);
        extra.put("actorUserId", Long.valueOf(actorUserId));
        return MessagePublishRequest.builder()
                .receiverUserIds(List.of(Long.valueOf(commentAuthorId)))
                .deliveryScope(MessageDeliveryScope.DIRECT)
                .messageType(MessageType.RESOURCE_INTERACTION)
                .title("评论被点赞")
                .content(MessageTemplatePlaceholders.user(actorUserId) + " 点赞了你在资源 " + resource.getResourceName() + " 下的评论")
                .jumpUrl(null)
                .sourceService(BusinessDomain.RESOURCE)
                .bizTraceId("RESOURCE_INTERACTION:COMMENT_LIKE:" + resource.getResourceId() + ":" + commentId + ":" + actorUserId + ":" + IdUtil.fastSimpleUUID())
                .extra(JSONUtil.toJsonStr(extra))
                .build();
    }

    public static MessagePublishRequest inlineComment(ResourceItemEntity resource, String actorUserId,
                                                      String inlineCommentId, String itemId, String receiverUserId,
                                                      String content) {
        String commentContent = content == null ? "" : content;
        if (commentContent.length() > 80) {
            commentContent = commentContent.substring(0, 80);
        }
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("actionType", "INLINE_COMMENT");
        extra.put("resourceId", resource.getResourceId());
        extra.put("inlineCommentId", inlineCommentId);
        extra.put("itemId", itemId);
        extra.put("actorUserId", Long.valueOf(actorUserId));
        return MessagePublishRequest.builder()
                .receiverUserIds(List.of(Long.valueOf(receiverUserId)))
                .deliveryScope(MessageDeliveryScope.DIRECT)
                .messageType(MessageType.RESOURCE_INTERACTION)
                .title("资源收到新行内评论")
                .content(MessageTemplatePlaceholders.user(actorUserId) + " 在资源 " + resource.getResourceName() + " 中发表了行内评论：" + commentContent)
                .jumpUrl(null)
                .sourceService(BusinessDomain.RESOURCE)
                .bizTraceId("RESOURCE_INTERACTION:INLINE_COMMENT:" + resource.getResourceId() + ":" + inlineCommentId + ":" + itemId)
                .extra(JSONUtil.toJsonStr(extra))
                .build();
    }

    public static MessagePublishRequest inlineCommentReaction(ResourceItemEntity resource, String actorUserId,
                                                              String inlineCommentId, String itemId, String emojiId,
                                                              String receiverUserId, String content) {
        String commentContent = content == null ? "" : content;
        if (commentContent.length() > 80) {
            commentContent = commentContent.substring(0, 80);
        }
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("actionType", "INLINE_COMMENT_REACTION");
        extra.put("resourceId", resource.getResourceId());
        extra.put("inlineCommentId", inlineCommentId);
        extra.put("itemId", itemId);
        extra.put("emojiId", emojiId);
        extra.put("actorUserId", Long.valueOf(actorUserId));
        return MessagePublishRequest.builder()
                .receiverUserIds(List.of(Long.valueOf(receiverUserId)))
                .deliveryScope(MessageDeliveryScope.DIRECT)
                .messageType(MessageType.RESOURCE_INTERACTION)
                .title("行内评论收到回应")
                .content(MessageTemplatePlaceholders.user(actorUserId) + " 回应了你在资源 " + resource.getResourceName() + " 中的行内评论：" + commentContent)
                .jumpUrl(null)
                .sourceService(BusinessDomain.RESOURCE)
                .bizTraceId("RESOURCE_INTERACTION:INLINE_COMMENT_REACTION:" + resource.getResourceId() + ":" + inlineCommentId + ":" + itemId + ":" + emojiId + ":" + actorUserId + ":" + IdUtil.fastSimpleUUID())
                .extra(JSONUtil.toJsonStr(extra))
                .build();
    }
}
