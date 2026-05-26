package com.oriole.wisepen.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.note.api.constant.NoteForkFailureDetail;
import com.oriole.wisepen.note.api.domain.base.NoteInfoBase;
import com.oriole.wisepen.note.api.domain.dto.req.NoteCreateRequest;
import com.oriole.wisepen.note.domain.entity.NoteInfoEntity;
import com.oriole.wisepen.note.domain.entity.NoteVersionEntity;
import com.oriole.wisepen.note.exception.NoteError;
import com.oriole.wisepen.note.mq.KafkaNoteEventPublisher;
import com.oriole.wisepen.note.repository.NoteDocumentRepository;
import com.oriole.wisepen.note.repository.NoteVersionRepository;
import com.oriole.wisepen.note.service.INoteOperationLogService;
import com.oriole.wisepen.note.service.INoteService;
import com.oriole.wisepen.note.service.INoteVersionService;
import com.oriole.wisepen.resource.domain.dto.ResourceCreateReqDTO;
import com.oriole.wisepen.resource.domain.mq.ResourceForkCompletedMessage;
import com.oriole.wisepen.resource.domain.mq.ResourceForkMessage;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements INoteService {

    private final NoteDocumentRepository noteDocumentRepository;
    private final NoteVersionRepository noteVersionRepository;

    private final INoteVersionService noteVersionService;
    private final INoteOperationLogService noteOperationLogService;
    private final RemoteResourceService remoteResourceService;
    private final KafkaNoteEventPublisher noteEventPublisher;

    @Override
    public String createNote(NoteCreateRequest request, String userId) {
        // 远端请求创建 Note
        ResourceCreateReqDTO resourceReq = ResourceCreateReqDTO.builder()
                .resourceName(request.getTitle())
                .resourceType(ResourceType.NOTE)
                .ownerId(userId)
                .originalEditorIds(List.of(userId))
                .build();
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
                .orElseThrow(() -> new ServiceException(NoteError.NOTE_NOT_FOUND));
        return BeanUtil.copyProperties(noteInfoEntity, NoteInfoBase.class);
    }

    @Override
    public void forkNote(ResourceForkMessage message) {
        String sourceResourceId = message.getSourceResourceId();
        String newResourceId = message.getNewResourceId();
        try {
            if (noteDocumentRepository.findById(newResourceId).isPresent()) {
                publishForkCompleted(message, newResourceId, true, null);
                return;
            }

            NoteInfoEntity sourceInfo = noteDocumentRepository.findById(sourceResourceId).orElse(null);
            if (sourceInfo == null) {
                publishForkCompleted(message, newResourceId, false, NoteForkFailureDetail.SOURCE_NOTE_NOT_FOUND);
                return;
            }

            Long targetVersion = message.getVersion();
            List<NoteVersionEntity> sourceVersions = targetVersion == null
                    ? noteVersionRepository.findByResourceIdOrderByVersionAsc(sourceResourceId)
                    : noteVersionRepository.findByResourceIdAndVersionLessThanEqualOrderByVersionAsc(sourceResourceId, targetVersion);

            if (targetVersion != null) {
                boolean hasExactVersion = sourceVersions.stream().anyMatch(v -> targetVersion.equals(v.getVersion()));
                if (!hasExactVersion) {
                    publishForkCompleted(message, newResourceId, false, NoteForkFailureDetail.TARGET_VERSION_NOT_FOUND);
                    return;
                }
            }

            List<NoteVersionEntity> clonedVersions = sourceVersions.stream().map(version -> {
                NoteVersionEntity copied = BeanUtil.copyProperties(version, NoteVersionEntity.class);
                copied.setId(null);
                copied.setResourceId(newResourceId);
                return copied;
            }).toList();
            if (!clonedVersions.isEmpty()) {
                noteVersionRepository.saveAll(clonedVersions);
            }

            NoteInfoEntity newInfo = BeanUtil.copyProperties(sourceInfo, NoteInfoEntity.class);
            newInfo.setResourceId(newResourceId);
            newInfo.setLastUpdatedAt(LocalDateTime.now());
            newInfo.setAuthors(List.of(Long.valueOf(message.getOwnerId())));
            if (targetVersion != null) {
                newInfo.setPlainText(null);
            }
            noteDocumentRepository.save(newInfo);

            publishForkCompleted(message, newResourceId, true, null);
        } catch (Exception e) {
            log.warn("noteFork failed sourceResourceId={} newResourceId={} version={}",
                    sourceResourceId, newResourceId, message.getVersion(), e);
            publishForkCompleted(message, newResourceId, false, e.getMessage());
        }
    }

    private void publishForkCompleted(ResourceForkMessage message, String newResourceId, boolean success,
            String errorMessage) {
        noteEventPublisher.publishForkCompleted(
                ResourceForkCompletedMessage.fromFork(message, newResourceId, success, errorMessage, ResourceType.NOTE));
    }
}
