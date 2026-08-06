package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.resource.domain.dto.req.CommentCreateRequest;
import com.oriole.wisepen.resource.domain.dto.req.CommentReplyCreateRequest;
import com.oriole.wisepen.resource.domain.dto.req.CommentDeleteRequest;
import com.oriole.wisepen.resource.domain.dto.req.CommentLikeRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceCommentItemResponse;
import com.oriole.wisepen.resource.enums.CommentSortBy;

import java.util.Map;

public interface IResourceCommentService {

    String createComment(CommentCreateRequest request, String operatorUserId, Map<Long, GroupRoleType> groupRoles);

    String createReply(CommentReplyCreateRequest request, String operatorUserId, Map<Long, GroupRoleType> groupRoles);

    void deleteCommentItem(CommentDeleteRequest request, String operatorUserId, IdentityType operatorIdentityType, Map<Long, GroupRoleType> groupRoles);

    boolean toggleLike(CommentLikeRequest request, String operatorUserId);

    PageR<ResourceCommentItemResponse> listComments(String resourceId, CommentSortBy sortBy, int size, int page);

    PageR<ResourceCommentItemResponse> listReplies(String rootCommentId, int size, int page);
}
