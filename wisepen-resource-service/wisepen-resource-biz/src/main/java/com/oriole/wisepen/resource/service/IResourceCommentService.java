package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.resource.domain.dto.req.CommentCreateRequest;
import com.oriole.wisepen.resource.domain.dto.req.CommentReplyCreateRequest;
import com.oriole.wisepen.resource.domain.dto.req.CommentDeleteRequest;
import com.oriole.wisepen.resource.domain.dto.req.CommentLikeRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceCommentItemResponse;
import com.oriole.wisepen.resource.enums.CommentSortBy;

public interface IResourceCommentService {

    String createComment(CommentCreateRequest request, String operatorUserId);

    String createReply(CommentReplyCreateRequest request, String operatorUserId);

    void deleteCommentItem(CommentDeleteRequest request, String operatorUserId, IdentityType operatorIdentityType);

    boolean toggleLike(CommentLikeRequest request, String operatorUserId);

    PageR<ResourceCommentItemResponse> listComments(String resourceId, CommentSortBy sortBy, int size, int page);

    PageR<ResourceCommentItemResponse> listReplies(String rootCommentId, int size, int page);
}
