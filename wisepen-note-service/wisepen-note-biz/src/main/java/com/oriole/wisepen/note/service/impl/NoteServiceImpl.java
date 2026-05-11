package com.oriole.wisepen.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.note.api.domain.base.NoteInfoBase;
import com.oriole.wisepen.note.api.domain.dto.req.NoteCreateRequest;
import com.oriole.wisepen.note.api.domain.dto.req.NoteForkReqDTO;
import com.oriole.wisepen.note.domain.entity.NoteInfoEntity;
import com.oriole.wisepen.note.domain.entity.NoteVersionEntity;
import com.oriole.wisepen.note.api.domain.enums.VersionType;
import com.oriole.wisepen.note.exception.NoteErrorCode;
import com.oriole.wisepen.note.repository.NoteDocumentRepository;
import com.oriole.wisepen.note.repository.NoteVersionRepository;
import com.oriole.wisepen.note.service.INoteOperationLogService;
import com.oriole.wisepen.note.service.INoteService;
import com.oriole.wisepen.note.service.INoteVersionService;
import com.oriole.wisepen.resource.domain.dto.ResourceCreateReqDTO;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements INoteService {

    private final NoteDocumentRepository noteDocumentRepository;
    private final NoteVersionRepository noteVersionRepository;

    private final INoteVersionService noteVersionService;
    private final INoteOperationLogService noteOperationLogService;
    private final RemoteResourceService remoteResourceService;

    @Override
    public String createNote(NoteCreateRequest request, String userId) {
        // 远端请求创建 Note
        ResourceCreateReqDTO resourceReq = ResourceCreateReqDTO.builder()
                .resourceName(request.getTitle()).resourceType(ResourceType.NOTE).ownerId(userId).build();
        String resourceId = remoteResourceService.createResource(resourceReq).getData();

        List<Long> authors = new ArrayList<>();
        authors.add(Long.valueOf(userId));

        NoteInfoEntity doc = NoteInfoEntity.builder()
                .resourceId(resourceId)
                .lastUpdatedAt(LocalDateTime.now())
                .authors(authors)
                .build();
        noteDocumentRepository.save(doc);
        return resourceId;
    }

    @Override
    @Transactional
    public void deleteNotes(List<String> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }
        // 移除所有内容
        noteDocumentRepository.deleteByResourceIdIn(resourceIds);

        noteVersionService.deleteAllVersionsByResourceIds(resourceIds);
        noteOperationLogService.deleteAllOpLogsByResourceIds(resourceIds);
    }

    @Override
    public NoteInfoBase getNoteInfo(String resourceId) {
        NoteInfoEntity noteInfoEntity = noteDocumentRepository.findByResourceId(resourceId)
                .orElseThrow(() -> new ServiceException(NoteErrorCode.NOTE_NOT_FOUND));
        return BeanUtil.copyProperties(noteInfoEntity, NoteInfoBase.class);
    }

    @Override
    @Transactional
    public void forkNote(NoteForkReqDTO req) {
        String originalId = req.getOriginalResourceId();
        String newId = req.getNewResourceId();
        Long newOwnerId = req.getNewOwnerId();

        // 获取并拷贝原笔记的 Info 元数据
        NoteInfoEntity originalInfo = noteDocumentRepository.findByResourceId(originalId)
                .orElseThrow(() -> new ServiceException(NoteErrorCode.NOTE_NOT_FOUND));

        NoteInfoEntity newInfo = BeanUtil.copyProperties(originalInfo, NoteInfoEntity.class);
        newInfo.setResourceId(newId);
        newInfo.setAuthors(Collections.singletonList(newOwnerId));
        newInfo.setLastUpdatedAt(LocalDateTime.now());
        noteDocumentRepository.save(newInfo);


        Optional<NoteVersionEntity> latestFullVersionOpt = noteVersionRepository
                .findFirstByResourceIdAndTypeOrderByVersionDesc(originalId, VersionType.FULL);
        Long latestFullVersion = latestFullVersionOpt.map(NoteVersionEntity::getVersion).orElse(0L);
        List<NoteVersionEntity> versionsToCopy = new ArrayList<>();
        latestFullVersionOpt.ifPresent(versionsToCopy::add);
        List<NoteVersionEntity> deltaVersions = noteVersionRepository
                .findByResourceIdAndVersionGreaterThanAndTypeOrderByVersionAsc(
                        originalId, latestFullVersion, VersionType.DELTA);
        versionsToCopy.addAll(deltaVersions);

        // 插入版本记录
        List<NoteVersionEntity> newVersions = versionsToCopy.stream().map(v -> {
            NoteVersionEntity newV = BeanUtil.copyProperties(v, NoteVersionEntity.class);
            newV.setId(null);
            newV.setResourceId(newId);
            newV.setCreatedBy(Collections.singletonList(newOwnerId));
            newV.setCreatedAt(LocalDateTime.now());
            return newV;
        }).toList();

        if (!newVersions.isEmpty()) {
            noteVersionRepository.saveAll(newVersions);
        }
    }
}
