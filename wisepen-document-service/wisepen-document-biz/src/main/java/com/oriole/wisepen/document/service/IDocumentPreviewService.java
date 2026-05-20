package com.oriole.wisepen.document.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IDocumentPreviewService {
    // 处理预览请求
    void handlePreviewRequest(HttpServletRequest request,
                              HttpServletResponse response,
                              String documentId,
                              String userId);
}
