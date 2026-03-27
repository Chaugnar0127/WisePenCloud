package com.oriole.wisepen.note.service.impl;

import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.note.api.domain.dto.res.NoteVersionListResponse;
import com.oriole.wisepen.note.domain.entity.NoteDocumentEntity;
import com.oriole.wisepen.note.domain.entity.NoteVersionEntity;
import com.oriole.wisepen.note.domain.enums.VersionType;
import com.oriole.wisepen.note.exception.NoteErrorCode;
import com.oriole.wisepen.note.repository.NoteDocumentRepository;
import com.oriole.wisepen.note.repository.NoteVersionRepository;
import com.oriole.wisepen.note.service.INoteVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteVersionServiceImpl implements INoteVersionService {

    private final NoteVersionRepository noteVersionRepository;
    private final NoteDocumentRepository noteDocumentRepository;

    @Override
    public void createVersion(String resourceId, Long version, VersionType type,
                              byte[] data, String label, String userId) {
        NoteVersionEntity entity = new NoteVersionEntity();
        entity.setResourceId(resourceId);
        entity.setVersion(version);
        entity.setType(type);
        entity.setData(new Binary(data));
        entity.setLabel(label);
        entity.setCreatedAt(new Date());
        entity.setCreatedBy(userId);
        noteVersionRepository.save(entity);
    }

    @Override
    public List<byte[]> findDeltasAfterLatestFull(String resourceId) {
        Optional<NoteVersionEntity> latestFull = noteVersionRepository
                .findFirstByResourceIdAndTypeOrderByVersionDesc(resourceId, VersionType.FULL);
        Long fullVersion = latestFull.map(NoteVersionEntity::getVersion).orElse(0L);
        List<NoteVersionEntity> deltas = noteVersionRepository
                .findByResourceIdAndVersionGreaterThanAndTypeOrderByVersionAsc(
                        resourceId, fullVersion, VersionType.DELTA);
        System.out.println(deltas);
        return deltas.stream()
                .map(e -> e.getData().getData())
                .toList();
    }

    @Override
    public Page<NoteVersionListResponse> listVersions(String resourceId, int page, int size) {
        Page<NoteVersionEntity> entities = noteVersionRepository
                .findByResourceIdOrderByVersionDesc(resourceId, PageRequest.of(page - 1, size));
        return entities.map(e -> {
            NoteVersionListResponse resp = new NoteVersionListResponse();
            resp.setId(e.getId());
            resp.setVersion(e.getVersion());
            resp.setType(e.getType().name());
            resp.setLabel(e.getLabel());
            resp.setCreatedAt(e.getCreatedAt());
            resp.setCreatedBy(e.getCreatedBy());
            return resp;
        });
    }

    @Override
    public void saveManualVersion(String resourceId, String label, String userId) {
        NoteDocumentEntity doc = noteDocumentRepository.findByResourceId(resourceId)
                .orElseThrow(() -> new ServiceException(NoteErrorCode.NOTE_NOT_FOUND));
        if (doc.getFullSnapshot() == null) {
            throw new ServiceException(NoteErrorCode.NOTE_SNAPSHOT_EMPTY);
        }
        createVersion(resourceId, doc.getVersion(), VersionType.FULL,
                doc.getFullSnapshot().getData(),
                label, userId);
    }

    @Override
    public byte[][] getVersionChainForRevert(String resourceId, Long targetVersion) {
        NoteVersionEntity checkpoint = noteVersionRepository
                .findFirstByResourceIdAndTypeAndVersionLessThanEqualOrderByVersionDesc(
                        resourceId, VersionType.FULL, targetVersion)
                .orElseThrow(() -> new ServiceException(NoteErrorCode.NOTE_CHECKPOINT_MISSING));

        List<NoteVersionEntity> deltas = noteVersionRepository
                .findByResourceIdAndVersionBetweenOrderByVersionAsc(
                        resourceId, checkpoint.getVersion() + 1, targetVersion);

        byte[][] chain = new byte[1 + deltas.size()][];
        chain[0] = checkpoint.getData().getData();
        for (int i = 0; i < deltas.size(); i++) {
            chain[i + 1] = deltas.get(i).getData().getData();
        }
        return chain;
    }

    @Override
    public void deleteByResourceId(String resourceId) {
        noteVersionRepository.deleteByResourceId(resourceId);
    }
}
