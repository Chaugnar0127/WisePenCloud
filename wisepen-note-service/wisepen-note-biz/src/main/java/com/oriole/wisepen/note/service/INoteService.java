package com.oriole.wisepen.note.service;

import com.oriole.wisepen.note.api.domain.dto.req.NoteCreateRequest;
import com.oriole.wisepen.note.api.domain.dto.res.NoteInfoResponse;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;

public interface INoteService {

    String createNote(NoteCreateRequest request, String userId);

    void deleteNote(String resourceId, String userId);

    NoteInfoResponse getNoteInfo(String resourceId);

    NoteSnapshotResponse getLatestSnapshot(String resourceId);

    ResourceCheckPermissionResDTO checkPermission(ResourceCheckPermissionReqDTO dto);

    void upsertSnapshot(String resourceId, byte[] fullSnapshot, Long version, String updatedBy, String plainText);
}
