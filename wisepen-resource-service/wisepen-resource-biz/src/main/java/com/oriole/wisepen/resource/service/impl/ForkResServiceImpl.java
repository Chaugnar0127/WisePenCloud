package com.oriole.wisepen.resource.service.impl;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.document.api.domain.dto.req.DocumentForkReqDTO;
import com.oriole.wisepen.document.api.domain.dto.DocumentInternalInfoDTO;
import com.oriole.wisepen.document.api.feign.RemoteDocumentService;
import com.oriole.wisepen.file.storage.api.domain.dto.StorageRecordDTO;
import com.oriole.wisepen.file.storage.api.feign.RemoteStorageService;
import com.oriole.wisepen.note.api.domain.dto.req.NoteForkReqDTO;
import com.oriole.wisepen.note.api.feign.RemoteNoteService;
import com.oriole.wisepen.resource.domain.dto.ResourceCreateReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceForkReqDTO;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.exception.ResourceError;
import com.oriole.wisepen.resource.repository.ResourceItemRepository;
import com.oriole.wisepen.resource.service.IForkResService;
import com.oriole.wisepen.resource.service.IResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForkResServiceImpl implements IForkResService {
    private final ResourceItemRepository resourceItemRepository;
    private final IResourceService resourceService;
    private final RemoteDocumentService remoteDocumentService;
    private final RemoteStorageService remoteStorageService;
    private final RemoteNoteService remoteNoteService;

    @Override
    public String forkRes(ResourceForkReqDTO req) {
        String resId = req.getResourceId();
        ResourceItemEntity originalItem = resourceItemRepository.findById(resId)
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));

        if (req.getResourceType() == ResourceType.NOTE) {
            return forkNote(req, originalItem);
        }
        if (req.getResourceType().isOffice()) {
            return forkDocument(req, originalItem);
        }
        throw new ServiceException(ResourceError.RESOURCE_PERMISSION_DENIED, "该类型资源不支持fork");
    }

    @Override
    public String forkSnapshot(ResourceForkReqDTO req) {
        throw new ServiceException(ResourceError.RESOURCE_MARKET_OPERATION_UNSUPPORTED);
    }

    private String forkNote(ResourceForkReqDTO req, ResourceItemEntity originalItem) {

        ResourceCreateReqDTO createReqDTO = ResourceCreateReqDTO.builder()
                .resourceName(originalItem.getResourceName())
                .resourceType(originalItem.getResourceType())
                .ownerId(req.getNewOwnerId())
                .size(originalItem.getSize())
                .preview(originalItem.getPreview())
                .build();
        String newResourceId = resourceService.createResourceItem(createReqDTO);


        NoteForkReqDTO noteForkReq = NoteForkReqDTO.builder()
                .originalResourceId(req.getResourceId())
                .newResourceId(newResourceId)
                .newOwnerId(Long.valueOf(req.getNewOwnerId()))
                .build();
        R<Void> forkR = remoteNoteService.forkNote(noteForkReq);
        if (forkR.getCode() != 200) {
            throw new ServiceException(ResourceError.RESOURCE_NOT_FOUND, "克隆笔记数据失败");
        }

        ResourceItemEntity newEntity = resourceItemRepository.findById(newResourceId).orElseThrow();
        newEntity.setOverrideGrantedActionsMask(req.getTier().getPermissionMask());
        resourceItemRepository.save(newEntity);

        log.info("Note fork成功: oldId={} newId={}", req.getResourceId(), newResourceId);
        return newResourceId;
    }

    private String forkDocument(ResourceForkReqDTO req, ResourceItemEntity originalItem) {

        R<DocumentInternalInfoDTO> docInfoR = remoteDocumentService.getInternalDocumentInfo(req.getResourceId());
        if (docInfoR.getCode() != 200 || docInfoR.getData() == null) {
            throw new ServiceException(ResourceError.RESOURCE_NOT_FOUND, "无法获取原文档的底层数据");
        }
        DocumentInternalInfoDTO docInfo = docInfoR.getData();


        R<StorageRecordDTO> sourceCopyR = remoteStorageService.copyFile(docInfo.getSourceObjectKey());
        if (sourceCopyR.getCode() != 200 || sourceCopyR.getData() == null) {
            throw new ServiceException(ResourceError.RESOURCE_NOT_FOUND, "克隆源文件失败");
        }
        String newSourceObjectKey = sourceCopyR.getData().getObjectKey();

        String newPreviewObjectKey = null;
        if (docInfo.getPreviewObjectKey() != null) {
            R<StorageRecordDTO> previewCopyR = remoteStorageService.copyFile(docInfo.getPreviewObjectKey());
            if (previewCopyR.getCode() == 200 && previewCopyR.getData() != null) {
                newPreviewObjectKey = previewCopyR.getData().getObjectKey();
            }
        }


        ResourceCreateReqDTO createReqDTO = ResourceCreateReqDTO.builder()
                .resourceName(originalItem.getResourceName())
                .resourceType(originalItem.getResourceType())
                .ownerId(req.getNewOwnerId())
                .size(originalItem.getSize())
                .preview(originalItem.getPreview())
                .build();
        String newResourceId = resourceService.createResourceItem(createReqDTO);


        DocumentForkReqDTO docForkReq = DocumentForkReqDTO.builder()
                .originalResourceId(req.getResourceId())
                .newResourceId(newResourceId)
                .newOwnerId(Long.valueOf(req.getNewOwnerId()))
                .newSourceObjectKey(newSourceObjectKey)
                .newPreviewObjectKey(newPreviewObjectKey)
                .build();
        remoteDocumentService.forkDocumentInfo(docForkReq);


        ResourceItemEntity newEntity = resourceItemRepository.findById(newResourceId).orElseThrow();
        newEntity.setOverrideGrantedActionsMask(req.getTier().getPermissionMask());
        resourceItemRepository.save(newEntity);

        log.info("Document fork成功: oldId={} newId={}", req.getResourceId(), newResourceId);
        return newResourceId;
    }
}
