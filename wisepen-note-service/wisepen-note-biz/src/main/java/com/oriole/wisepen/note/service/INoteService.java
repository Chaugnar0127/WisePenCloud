package com.oriole.wisepen.note.service;

import com.oriole.wisepen.note.api.domain.base.NoteInfoBase;
import com.oriole.wisepen.note.api.domain.dto.req.NoteCreateRequest;

public interface INoteService {

    String createNote(NoteCreateRequest request, String userId);

    void deleteNote(String resourceId);

    NoteInfoBase getNoteInfo(String resourceId);
}
