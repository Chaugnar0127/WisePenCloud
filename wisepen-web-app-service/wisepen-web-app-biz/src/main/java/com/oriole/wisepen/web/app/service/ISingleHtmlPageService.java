package com.oriole.wisepen.web.app.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.web.app.domain.dto.res.SingleHtmlPageVersionInfoResponse;
import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageEntity;
import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageVersionEntity;

import java.util.List;

public interface ISingleHtmlPageService {
    SingleHtmlPageEntity getSingleHtmlPageInfo(String resourceId);

    SingleHtmlPageVersionEntity getSingleHtmlPageVersion(String resourceId, Integer version);

    String getSingleHtmlPageContent(String resourceId, Integer version);

    PageR<SingleHtmlPageVersionInfoResponse> listVersions(String resourceId, int page, int size);

    void deletePages(List<String> resourceIds);
}
