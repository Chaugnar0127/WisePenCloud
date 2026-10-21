package com.oriole.wisepen.web.app.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.web.app.domain.base.SingleHtmlPageVersionBase;
import com.oriole.wisepen.web.app.domain.dto.req.SingleHtmlPageCreateRequest;
import com.oriole.wisepen.web.app.domain.dto.req.SingleHtmlPageUploadInitRequest;
import com.oriole.wisepen.web.app.domain.dto.res.SingleHtmlPageUploadInitResponse;
import com.oriole.wisepen.web.app.domain.dto.res.SingleHtmlPageVersionInfoResponse;
import com.oriole.wisepen.web.app.enums.SingleHtmlPageStatus;
import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageEntity;
import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageVersionEntity;

import java.util.List;
import java.util.Map;

public interface ISingleHtmlPageService {
    String createSingleHtmlPage(SingleHtmlPageCreateRequest request, Long ownerUserId,
                                Map<Long, GroupRoleType> ownerGroupRoles);

    SingleHtmlPageUploadInitResponse initUploadSingleHtmlPage(
            SingleHtmlPageUploadInitRequest request, Long uploaderUserId, Map<Long, GroupRoleType> uploaderGroupRoles);

    void finalizeToReady(String singleHtmlPageId);

    void assertVersionUploader(String singleHtmlPageId, Long uploaderUserId);

    SingleHtmlPageStatus refreshSingleHtmlPageStatus(String singleHtmlPageId);

    SingleHtmlPageEntity getSingleHtmlPageInfo(String resourceId);

    SingleHtmlPageVersionEntity getSingleHtmlPageVersion(String resourceId, Integer version);

    String getSingleHtmlPageContent(String resourceId, Integer version);

    PageR<SingleHtmlPageVersionInfoResponse> listVersions(String resourceId, int page, int size);

    List<SingleHtmlPageVersionBase> listPending(Long uploaderUserId);

    void deletedSingleHtmlPageVersion(String singleHtmlPageId);

    void deletePages(List<String> resourceIds);
}
