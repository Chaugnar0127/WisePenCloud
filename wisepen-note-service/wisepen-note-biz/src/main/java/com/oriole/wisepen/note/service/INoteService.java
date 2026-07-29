package com.oriole.wisepen.note.service;

import com.oriole.wisepen.note.api.domain.dto.req.NoteCreateRequest;
import com.oriole.wisepen.note.api.domain.dto.req.NoteForkRequest;
import com.oriole.wisepen.note.domain.entity.NoteInfoEntity;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;

import java.util.List;
import java.util.Map;

public interface INoteService {

    String createNote(NoteCreateRequest request, String userId, Map<Long, GroupRoleType> groupRoles);

    void deleteNotes(List<String> resourceIds);

    NoteInfoEntity getNoteInfo(String resourceId);

    String forkNote(NoteForkRequest request, String forkedResourceOwnerId);
}
