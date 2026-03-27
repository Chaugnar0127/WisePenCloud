package com.oriole.wisepen.note.service.impl;

import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.note.api.domain.dto.req.NoteCreateRequest;
import com.oriole.wisepen.note.api.domain.dto.res.NoteInfoResponse;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import com.oriole.wisepen.note.domain.entity.NoteDocumentEntity;
import com.oriole.wisepen.note.exception.NoteErrorCode;
import com.oriole.wisepen.note.repository.NoteDocumentRepository;
import com.oriole.wisepen.note.service.INoteOperationLogService;
import com.oriole.wisepen.note.service.INoteService;
import com.oriole.wisepen.note.service.INoteVersionService;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCreateReqDTO;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements INoteService {

    private final NoteDocumentRepository noteDocumentRepository;
    private final INoteVersionService noteVersionService;
    private final INoteOperationLogService noteOperationLogService;
    private final RemoteResourceService remoteResourceService;

    @Override
    public String createNote(NoteCreateRequest request, String userId) {
        ResourceCreateReqDTO resourceReq = ResourceCreateReqDTO.builder()
                .resourceName(request.getTitle())
                .resourceType("NOTE")
                .ownerId(userId)
                .build();
        String resourceId = remoteResourceService.createResource(resourceReq).getData();

        NoteDocumentEntity doc = new NoteDocumentEntity();
        doc.setResourceId(resourceId);
        doc.setVersion(0L);
        doc.setLastUpdatedAt(new Date());
        doc.setLastUpdatedBy(userId);
        noteDocumentRepository.save(doc);

        return resourceId;
    }

    @Override
    public void deleteNote(String resourceId, String userId) {
        noteDocumentRepository.deleteByResourceId(resourceId);
        noteVersionService.deleteByResourceId(resourceId);
        noteOperationLogService.deleteByResourceId(resourceId);
        remoteResourceService.removeResource(resourceId);
    }

    @Override
    public NoteInfoResponse getNoteInfo(String resourceId) {
        NoteDocumentEntity doc = noteDocumentRepository.findByResourceId(resourceId)
                .orElseThrow(() -> new ServiceException(NoteErrorCode.NOTE_NOT_FOUND));
        NoteInfoResponse resp = new NoteInfoResponse();
        resp.setResourceId(doc.getResourceId());
        resp.setVersion(doc.getVersion());
        resp.setLastUpdatedAt(doc.getLastUpdatedAt());
        resp.setLastUpdatedBy(doc.getLastUpdatedBy());
        return resp;
    }

    @Override
    public NoteSnapshotResponse getLatestSnapshot(String resourceId) {
        NoteDocumentEntity doc = noteDocumentRepository.findByResourceId(resourceId)
                .orElseThrow(() -> new ServiceException(NoteErrorCode.NOTE_NOT_FOUND));
        List<byte[]> deltaBytes = noteVersionService.findDeltasAfterLatestFull(resourceId);
        List<String> deltas = deltaBytes.stream()
                .map(b -> Base64.getEncoder().encodeToString(b))
                .toList();

        long actualVersion = doc.getVersion() + deltaBytes.size();

        return NoteSnapshotResponse.builder()
                .resourceId(doc.getResourceId())
                .fullSnapshot(doc.getFullSnapshot() != null
                        ? Base64.getEncoder().encodeToString(doc.getFullSnapshot().getData()) : null)
                .version(actualVersion)
                .deltas(deltas.isEmpty() ? null : deltas)
                .build();
    }

    @Override
    public ResourceCheckPermissionResDTO checkPermission(ResourceCheckPermissionReqDTO dto) {
        return remoteResourceService.checkResPermission(dto).getData();
    }

    @Override
    public void upsertSnapshot(String resourceId, byte[] fullSnapshot, Long version,
                               String updatedBy, String plainText) {
        NoteDocumentEntity doc = noteDocumentRepository.findByResourceId(resourceId)
                .orElseGet(() -> {
                    NoteDocumentEntity newDoc = new NoteDocumentEntity();
                    newDoc.setResourceId(resourceId);
                    return newDoc;
                });

        doc.setFullSnapshot(new Binary(fullSnapshot));
        doc.setVersion(version);
        doc.setLastUpdatedAt(new Date());
        doc.setLastUpdatedBy(updatedBy);
        doc.setPlainText(plainText);
        noteDocumentRepository.save(doc);
    }
}
