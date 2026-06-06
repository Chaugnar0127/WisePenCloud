package com.oriole.wisepen.note.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import com.oriole.wisepen.note.api.domain.dto.res.NoteVersionListResponse;
import com.oriole.wisepen.note.api.domain.mq.NoteSnapshotMessage;
import com.oriole.wisepen.resource.domain.dto.req.ResourceForkRequest;

import java.util.List;

public interface INoteVersionService {

    void createVersion(NoteSnapshotMessage noteSnapshotMessage);

    NoteSnapshotResponse getLatestVersion(String resourceId);

    NoteSnapshotResponse getSnapshot(String resourceId, Long version);

    void forkNote(ResourceForkRequest request);

    PageR<NoteVersionListResponse> listVersions(String resourceId, int page, int size);

    void deleteAllVersionsByResourceIds(List<String> resourceIds);
}