package com.oriole.wisepen.document.service;

import com.onlyoffice.model.documenteditor.Callback;
import com.oriole.wisepen.document.api.domain.dto.res.OnlyOfficeEditorConfigResponse;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;

import java.util.Map;

public interface IOnlyOfficeEditService {

    OnlyOfficeEditorConfigResponse getEditorConfig(String resourceId, Long userId, ResourceCheckPermissionResDTO permissionResDTO, UserDisplayBase userDisplayInfo);

    Map<String, Integer> handleCallback(String sessionId, Callback request);

    void assertNoActiveEditSession(String resourceId);
}
