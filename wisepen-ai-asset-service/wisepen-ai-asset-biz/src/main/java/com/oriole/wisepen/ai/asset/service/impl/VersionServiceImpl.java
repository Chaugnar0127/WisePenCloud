package com.oriole.wisepen.ai.asset.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.oriole.wisepen.ai.asset.domain.base.AssetInfoBase;
import com.oriole.wisepen.ai.asset.domain.base.AssetPendingUploadBase;
import com.oriole.wisepen.ai.asset.domain.dto.req.AssetDeleteRequest;
import com.oriole.wisepen.ai.asset.domain.dto.req.AssetUploadCancelRequest;
import com.oriole.wisepen.ai.asset.domain.dto.req.AssetUploadInitRequest;
import com.oriole.wisepen.ai.asset.domain.dto.res.AssetUploadInitResponse;
import com.oriole.wisepen.ai.asset.domain.entity.AIResourceBaseEntity;
import com.oriole.wisepen.ai.asset.domain.entity.VersionBundleBaseEntity;
import com.oriole.wisepen.ai.asset.enums.AssetUploadStatus;
import com.oriole.wisepen.ai.asset.enums.VersionStatus;
import com.oriole.wisepen.ai.asset.exception.AIResourceError;
import com.oriole.wisepen.ai.asset.mq.AIAssetEventPublisher;
import com.oriole.wisepen.ai.asset.repository.AIResourceBaseRepository;
import com.oriole.wisepen.ai.asset.repository.CustomVersionBundleAssetRepository;
import com.oriole.wisepen.ai.asset.repository.VersionBundleBaseRepository;
import com.oriole.wisepen.ai.asset.service.IVersionService;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.file.storage.api.domain.dto.CopyReqDTO;
import com.oriole.wisepen.file.storage.api.domain.dto.StorageRecordDTO;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitReqDTO;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitRespDTO;
import com.oriole.wisepen.file.storage.api.domain.mq.FileUploadedMessage;
import com.oriole.wisepen.file.storage.api.enums.StorageSceneEnum;
import com.oriole.wisepen.file.storage.api.feign.RemoteStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public abstract class VersionServiceImpl<VT extends VersionBundleBaseEntity<VT>, AT extends AIResourceBaseEntity<AT>> implements IVersionService<VT> {

    private static final String ROOT_PATH = "/";

    final VersionBundleBaseRepository<VT> versionBundleBaseRepository;
    final AIResourceBaseRepository<AT> aiResourceBaseRepository;
    private final RemoteStorageService remoteStorageService;
    private final AIAssetEventPublisher eventPublisher;
    private final CustomVersionBundleAssetRepository versionBundleAssetRepository;

    protected abstract VT buildDraft(String resourceId, Integer draftVersion);

    protected abstract StorageSceneEnum getStorageScene();

    @Override
    public void createDraftVersion(String resourceId, Integer draftVersion) {
        VT draft = buildDraft(resourceId, draftVersion);
        // 如果不是首份草案(1)需要复制此前的资源列表
        if (draftVersion > 1) {
            versionBundleBaseRepository.findByResourceIdAndVersion(resourceId, draftVersion - 1).ifPresent(draft::copyBy);
        }
        versionBundleBaseRepository.save(draft);
    }

    @Override
    @Transactional
    public void forkPublishedVersionSnapshot(String sourceResourceId, Integer sourceVersion, String targetResourceId) {
        if (sourceVersion == null || sourceVersion <= 0) {
            throw new ServiceException(AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND);
        }
        VT source = versionBundleBaseRepository.findByResourceIdAndVersion(sourceResourceId, sourceVersion)
                .orElseThrow(() -> new ServiceException(AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND));
        if (source.getStatus() != VersionStatus.PUBLISHED) {
            throw new ServiceException(AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND);
        }

        List<String> copiedObjectKeys = new ArrayList<>();
        try {
            VT published = buildDraft(targetResourceId, 1);
            published.copyBy(source);
            published.setStatus(VersionStatus.PUBLISHED);
            // 资产文件需要物理拷贝
            published.setAssets(copyAssetsForFork(source.getAssets(), targetResourceId, copiedObjectKeys));
            published.checkCoreAssetReady();
            versionBundleBaseRepository.save(published);

            VT draft = buildDraft(targetResourceId, 2);
            draft.copyBy(published);
            draft.setStatus(VersionStatus.DRAFT);
            versionBundleBaseRepository.save(draft);
        } catch (Exception e) {
            cleanupForkedVersion(targetResourceId, copiedObjectKeys);
            throw new ServiceException(AIResourceError.AI_RESOURCE_FORK_FAILED, e.getMessage());
        }
    }

    private List<AssetInfoBase> copyAssetsForFork(List<AssetInfoBase> sourceAssets, String targetResourceId, List<String> copiedObjectKeys) {
        if (sourceAssets == null || sourceAssets.isEmpty()) {
            return new ArrayList<>();
        }
        List<AssetInfoBase> copiedAssets = new ArrayList<>();
        for (AssetInfoBase sourceAsset : sourceAssets) {
            if (isAssetUnavailable(sourceAsset)) {
                throw new ServiceException(AIResourceError.AI_RESOURCE_ASSET_NOT_READY);
            }
            StorageRecordDTO copied = remoteStorageService.copyFile(CopyReqDTO.builder()
                    .sourceObjectKey(sourceAsset.getObjectKey())
                    .scene(getStorageScene())
                    .bizTag(targetResourceId)
                    .build()).getData();
            if (copied == null || !StringUtils.hasText(copied.getObjectKey())) {
                throw new ServiceException(AIResourceError.AI_RESOURCE_FORK_FAILED);
            }
            copiedObjectKeys.add(copied.getObjectKey());
            copiedAssets.add(AssetInfoBase.builder()
                    .id(IdUtil.fastSimpleUUID())
                    .path(sourceAsset.getPath())
                    .name(sourceAsset.getName())
                    .assetResourceType(sourceAsset.getAssetResourceType())
                    .objectKey(copied.getObjectKey())
                    .size(copied.getSize())
                    .uploadStatus(AssetUploadStatus.AVAILABLE)
                    .build());
        }
        return copiedAssets;
    }

    private void cleanupForkedVersion(String targetResourceId, List<String> copiedObjectKeys) {
        versionBundleBaseRepository.deleteByResourceIdIn(List.of(targetResourceId));
        if (copiedObjectKeys != null && !copiedObjectKeys.isEmpty()) {
            eventPublisher.publishFileDeleteEvent(copiedObjectKeys);
        }
    }

    @Override
    public VT getVersionBundle(String resourceId, Integer version) {
        // 如果未指定版本号则使用当前发布的版本号
        if (version == null){
            AT entity = aiResourceBaseRepository.findByResourceId(resourceId)
                    .orElseThrow(() -> new ServiceException(AIResourceError.AI_RESOURCE_NOT_FOUND));
            version = entity.getVersion();
        }
        return versionBundleBaseRepository.findByResourceIdAndVersion(resourceId, version)
                .orElseThrow(() -> new ServiceException(AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND));
    }

    @Override
    public AssetUploadInitResponse initUploadAssets(AssetUploadInitRequest req) {
        VT draft = versionBundleBaseRepository.findByResourceIdAndVersion(req.getResourceId(), req.getDraftVersion())
                .orElseThrow(() -> new ServiceException(AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND));
        if (draft.getStatus() != VersionStatus.DRAFT) throw new ServiceException(AIResourceError.CANNOT_OPERATE_NON_DRAFT_AI_RESOURCE_VERSION);

        // 先完成本地校验，避免申请部分上传凭证后才发现后续资产参数非法
        for (AssetUploadInitRequest.AssetUploadRequest assetReq : req.getAssets()) {
            validateDirectoryPath(assetReq.getPath());
            validateFileName(assetReq.getName());
        }

        Set<String> replacedObjectKeys = new HashSet<>();
        AssetUploadInitResponse response = AssetUploadInitResponse.builder()
                .resourceId(req.getResourceId())
                .version(draft.getVersion())
                .build();

        for (AssetUploadInitRequest.AssetUploadRequest assetReq : req.getAssets()) {
            AssetInfoBase asset = findOrCreateDraftAsset(draft, assetReq.getPath(), assetReq.getName());

            UploadInitRespDTO uploadInitRespDTO;
            try {
                uploadInitRespDTO = remoteStorageService.initUpload(UploadInitReqDTO.builder()
                        .md5(assetReq.getMd5())
                        .extension(assetReq.getAssetResourceType().getExtension())
                        .scene(getStorageScene())
                        .bizTag(req.getResourceId())
                        .expectedSize(assetReq.getExpectedSize())
                        .isNeedCallback(true)
                        .build()).getData();
            } catch (Exception e) {
                log.warn("AI resource asset upload init failed. resourceId={} version={} dependency=storageService", req.getResourceId(), req.getDraftVersion(), e);
                throw new ServiceException(AIResourceError.AI_RESOURCE_ASSET_UPLOAD_URL_APPLY_FAILED);
            }

            if (Boolean.TRUE.equals(uploadInitRespDTO.getFlashUploaded())) {
                // 如果是快传，直接设置 AssetUploadStatus 为 AVAILABLE
                if (StringUtils.hasText(asset.getObjectKey())) replacedObjectKeys.add(asset.getObjectKey());
                asset.setObjectKey(uploadInitRespDTO.getObjectKey());
                asset.setAssetResourceType(assetReq.getAssetResourceType());
                asset.setSize(assetReq.getExpectedSize());
                asset.setUploadStatus(AssetUploadStatus.AVAILABLE);
                asset.setPendingUpload(null);
            } else {
                // 如果不是快传，则设置 AssetUploadStatus 为 UPLOADING
                asset.setPendingUpload(AssetPendingUploadBase.builder()
                        .objectKey(uploadInitRespDTO.getObjectKey())
                        .assetResourceType(assetReq.getAssetResourceType())
                        .expectedSize(assetReq.getExpectedSize())
                        .createTime(LocalDateTime.now())
                        .build());
                asset.setUploadStatus(AssetUploadStatus.UPLOADING);
            }

            AssetUploadInitResponse.AssetUploadTicket assetUploadTicket = AssetUploadInitResponse.AssetUploadTicket
                    .builder().assetId(asset.getId())
                    .path(asset.getPath())
                    .name(asset.getName()).build();
            BeanUtil.copyProperties(uploadInitRespDTO, assetUploadTicket);
            response.getAssetUploadTickets().add(assetUploadTicket);
        }

        versionBundleBaseRepository.save(draft);
        deleteUnreferencedObjectKeys(req.getResourceId(), replacedObjectKeys);
        return response;
    }

    private void validateDirectoryPath(String path) {
        if (path == null || path.isBlank() || !path.startsWith("/") || path.contains("\\")
                || path.contains("//") || path.contains("/../") || path.endsWith("/..")
                || (!ROOT_PATH.equals(path) && path.endsWith("/"))) {
            throw new ServiceException(AIResourceError.AI_RESOURCE_ASSET_PATH_INVALID);
        }
    }

    private void validateFileName(String name) {
        if (name == null || name.isBlank() || name.contains("/") || name.contains("\\")
                || ".".equals(name) || "..".equals(name)) {
            throw new ServiceException(AIResourceError.AI_RESOURCE_ASSET_PATH_INVALID);
        }
    }

    private AssetInfoBase findOrCreateDraftAsset(VT draft, String path, String name) {
        if (draft.getAssets() == null) draft.setAssets(new ArrayList<>());
        AssetInfoBase asset = draft.getAssets().stream()
                .filter(item -> path.equals(item.getPath()) && name.equals(item.getName()))
                .findFirst().orElse(null);
        if (asset == null) {
            asset = AssetInfoBase.builder().id(IdUtil.fastSimpleUUID())
                    .path(path).name(name)
                    .uploadStatus(AssetUploadStatus.UPLOADING)
                    .build();
            draft.getAssets().add(asset);
        }
        return asset;
    }

    @Override
    public void cancelUploadAssets(AssetUploadCancelRequest req) {
        // 显式取消尚未完成的 pending 上传
        VT draft = versionBundleBaseRepository.findByResourceIdAndVersion(req.getResourceId(), req.getDraftVersion())
                .orElseThrow(() -> new ServiceException(AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND));
        // 取消上传会修改版本中的资产状态，只能操作草稿
        if (draft.getStatus() != VersionStatus.DRAFT) throw new ServiceException(AIResourceError.CANNOT_OPERATE_NON_DRAFT_AI_RESOURCE_VERSION);

        Set<String> canceledObjectKeys = new HashSet<>(); // 收集取消成功的 objectKey
        for (AssetUploadCancelRequest.PendingUploadRef upload : req.getUploads()) {
            // 取消 assetId 相同 && pendingUpload 存在 && pendingUpload.objectKey 相同的资源
            AssetInfoBase asset = draft.getAssets().stream()
                    .filter(item -> upload.getAssetId().equals(item.getId()))
                    .filter(item -> item.getPendingUpload() != null)
                    .filter(item -> upload.getObjectKey().equals(item.getPendingUpload().getObjectKey()))
                    .findFirst()
                    .orElse(null);
            if (asset == null) continue;

            boolean canceled = StringUtils.hasText(asset.getObjectKey())
                    ? versionBundleAssetRepository.clearPendingUpload(
                            draft, upload.getAssetId(), upload.getObjectKey()) // 替换已有文件
                    : versionBundleAssetRepository.removePendingAsset(
                            draft, upload.getAssetId(), upload.getObjectKey()); // 直接从 assets 中删除整个占位资产
            if (canceled) canceledObjectKeys.add(upload.getObjectKey());
        }
        // 对不再被任何版本引用的 key 发布文件删除事件
        deleteUnreferencedObjectKeys(req.getResourceId(), canceledObjectKeys);
    }

    @Override
    public void deleteAssets(AssetDeleteRequest req) {
        // 检查是否是草案版本
        VT draft = versionBundleBaseRepository.findByResourceIdAndVersion(req.getResourceId(), req.getDraftVersion())
                .orElseThrow(() -> new ServiceException(AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND));
        if (draft.getStatus() != VersionStatus.DRAFT) throw new ServiceException(AIResourceError.CANNOT_OPERATE_NON_DRAFT_AI_RESOURCE_VERSION);

        Set<String> removedObjectKeys = new HashSet<>();

        for (String assetId : req.getAssetIds()) {
            AssetInfoBase removed = draft.getAssets().stream()
                    .filter(asset -> assetId.equals(asset.getId()))
                    .findFirst()
                    .orElse(null);
            if (removed != null) {
                draft.getAssets().remove(removed);
                removedObjectKeys.add(removed.getObjectKey());
                if (removed.getPendingUpload() != null) {
                    // 把待上传的 Object 一起删掉
                    removedObjectKeys.add(removed.getPendingUpload().getObjectKey());
                }
            }
        }

        versionBundleBaseRepository.save(draft);
        deleteUnreferencedObjectKeys(req.getResourceId(), removedObjectKeys);
    }

    private void deleteUnreferencedObjectKeys(String resourceId, Set<String> objectKeys) {
        if (objectKeys.isEmpty()) return;

        Set<String> referencedObjectKeys = new HashSet<>();
        versionBundleBaseRepository.findByResourceId(resourceId).forEach(version -> {
            if (version.getAssets() != null) version.getAssets().forEach(asset -> {
                referencedObjectKeys.add(asset.getObjectKey());
                if (asset.getPendingUpload() != null) {
                    referencedObjectKeys.add(asset.getPendingUpload().getObjectKey());
                }
            });
        });

        List<String> deletableObjectKeys = objectKeys.stream()
                .filter(StringUtils::hasText)
                .filter(objectKey -> !referencedObjectKeys.contains(objectKey))
                .toList();

        if (!deletableObjectKeys.isEmpty()) {
            eventPublisher.publishFileDeleteEvent(deletableObjectKeys);
        }
    }

    @Override
    @Transactional
    public void publishVersion(String resourceId) {
        AT resourceEntity = aiResourceBaseRepository.findByResourceId(resourceId)
                .orElseThrow(() -> new ServiceException(AIResourceError.AI_RESOURCE_NOT_FOUND));

        int draftVersion = resourceEntity.getVersion() + 1;
        // 检查当否是草案版本
        VT draft = versionBundleBaseRepository.findByResourceIdAndVersion(resourceId, draftVersion)
                .orElseThrow(() -> new ServiceException(AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND));
        if (draft.getStatus() != VersionStatus.DRAFT) throw new ServiceException(AIResourceError.CANNOT_OPERATE_NON_DRAFT_AI_RESOURCE_VERSION);
        // 核心资源缺失校验
        draft.checkCoreAssetReady();
        // 资源未就绪（有上传中的资源）
        if (draft.getAssets() != null && draft.getAssets().stream().anyMatch(this::isAssetUnavailable)) {
            throw new ServiceException(AIResourceError.AI_RESOURCE_ASSET_NOT_READY);
        }
        draft.setStatus(VersionStatus.PUBLISHED);
        aiResourceBaseRepository.updateVersionByResourceId(resourceId, draftVersion, LocalDateTime.now());
        versionBundleBaseRepository.save(draft);

        // 新草案是 version + 1，直接新建
        createDraftVersion(resourceId, draftVersion + 1);
    }

    private boolean isAssetUnavailable(AssetInfoBase asset) {
        return asset == null
                || !StringUtils.hasText(asset.getObjectKey())
                || asset.getUploadStatus() != AssetUploadStatus.AVAILABLE
                || asset.getPendingUpload() != null;
    }

    @Override
    public void handleFileUploaded(FileUploadedMessage msg) {
        if (msg.getScene() != getStorageScene()){ // 过滤不属于当前资产类型的消息
            return;
        }
        // 按正式 key 或 pending key 查找版本
        VT versionBundle = versionBundleBaseRepository.findFirstByAssetOrPendingObjectKey(msg.getObjectKey()).orElse(null);
        if (versionBundle == null) {
            if (Boolean.TRUE.equals(msg.getFlashUploaded())) {
                // 秒传事件可能早于上传初始化落库，初始化流程会同步提交秒传结果，此处跳过即可
                log.debug("ai resource flash upload event skipped. objectKey={} reason=\"upload initialization not persisted\"", msg.getObjectKey());
            } else {
                eventPublisher.publishFileDeleteEvent(List.of(msg.getObjectKey()));
                log.warn("ai resource stale asset upload detected. objectKey={}", msg.getObjectKey());
            }
            return;
        }

        AssetInfoBase availableAsset = versionBundle.getAssets().stream()
                .filter(item -> msg.getObjectKey().equals(item.getObjectKey()))
                .findFirst()
                .orElse(null);
        if (availableAsset != null) return; // 查到了正式文件，可能是重复投递或多实例同时处理，直接返回不处理

        AssetInfoBase pendingAsset = versionBundle.getAssets().stream()
                .filter(item -> item.getPendingUpload() != null)
                .filter(item -> msg.getObjectKey().equals(item.getPendingUpload().getObjectKey()))
                .findFirst()
                .orElse(null);

        // 保存旧正式 key 和 pending 元数据
        String oldObjectKey = pendingAsset.getObjectKey();
        AssetPendingUploadBase pendingUpload = pendingAsset.getPendingUpload();

        // 改变资源状态为 AVAILABLE
        boolean promoted = versionBundleAssetRepository.promotePendingUpload(
                versionBundle,
                pendingAsset.getId(),
                msg.getObjectKey(),
                pendingUpload.getAssetResourceType(),
                msg.getSize()
        );

        if (!promoted) { // 改变资源状态未能成功
            VT currentVersion = versionBundleBaseRepository.findFirstByAssetOrPendingObjectKey(msg.getObjectKey()).orElse(null);
            boolean alreadyAvailable = currentVersion != null && currentVersion.getAssets().stream()
                    .anyMatch(item -> msg.getObjectKey().equals(item.getObjectKey()));
            if (!alreadyAvailable && !Boolean.TRUE.equals(msg.getFlashUploaded())) {
                // 未 AVAILABLE，说明这个上传在处理期间可能取消或新上传替换
                // 发布删除事件
                eventPublisher.publishFileDeleteEvent(List.of(msg.getObjectKey()));
            }
            return;
        }

        if (StringUtils.hasText(oldObjectKey)) {
            // 删除旧文件
            deleteUnreferencedObjectKeys(versionBundle.getResourceId(), Set.of(oldObjectKey));
        }
        log.info("ai resource asset upload promoted. resourceId={} version={} assetId={} objectKey={}",
                versionBundle.getResourceId(), versionBundle.getVersion(), pendingAsset.getId(), msg.getObjectKey());
    }

    @Override
    @Transactional
    public void deleteAllVersionsByResourceIds(List<String> resourceIds) {
        Set<String> objectKeys = new HashSet<>();
        for (String resourceId : resourceIds) {
            List<VT> versionBundles = versionBundleBaseRepository.findByResourceId(resourceId);
            if (versionBundles.isEmpty()) continue;
            versionBundles.forEach(versionBundle -> {
                if (versionBundle.getAssets() != null) versionBundle.getAssets().forEach(asset -> {
                    objectKeys.add(asset.getObjectKey());
                    if (asset.getPendingUpload() != null) {
                        objectKeys.add(asset.getPendingUpload().getObjectKey());
                    }
                });
            });
        }
        versionBundleBaseRepository.deleteByResourceIdIn(resourceIds);
        List<String> deletableObjectKeys = objectKeys.stream().filter(StringUtils::hasText).toList();
        if (!deletableObjectKeys.isEmpty()) {
            eventPublisher.publishFileDeleteEvent(deletableObjectKeys);
        }
    }
}
