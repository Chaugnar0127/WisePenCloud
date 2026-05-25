package com.oriole.wisepen.skill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitRespDTO;
import com.oriole.wisepen.skill.domain.dto.SkillAssetUploadInitReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillCreateReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillInfoRespDTO;
import com.oriole.wisepen.skill.domain.dto.SkillManifestUploadInitReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillUpdateReqDTO;
import com.oriole.wisepen.skill.domain.entity.SkillAssetEntity;
import com.oriole.wisepen.skill.domain.entity.SkillEntity;
import com.oriole.wisepen.skill.domain.entity.SkillVersionEntity;
import com.oriole.wisepen.skill.domain.base.SkillAssetMetaBase;
import com.oriole.wisepen.skill.domain.base.SkillVersionBase;
import com.oriole.wisepen.skill.enums.SkillAuditStatusEnum;
import com.oriole.wisepen.skill.enums.SkillStatusEnum;
import com.oriole.wisepen.skill.exception.SkillError;
import com.oriole.wisepen.skill.repository.SkillRepository;
import com.oriole.wisepen.skill.service.ISkillService;
import com.oriole.wisepen.skill.service.ISkillStorageService;
import com.oriole.wisepen.resource.domain.dto.ResourceCreateReqDTO;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements ISkillService {

    private final SkillRepository skillRepository;
    private final RemoteResourceService remoteResourceService;
    private final ISkillStorageService skillStorageService;

    @Override
    public String createSkill(SkillCreateReqDTO dto, String userId) {
        ResourceCreateReqDTO resourceReq = ResourceCreateReqDTO.builder()
                .resourceName(dto.getSkillName())
                .resourceType(ResourceType.SKILL)
                .ownerId(userId)
                .build();
        String skillId = remoteResourceService.createResource(resourceReq).getData();
        if (!StringUtils.hasText(skillId)) {
            throw new ServiceException(SkillError.SKILL_RESOURCE_REGISTER_FAILED);
        }

        SkillEntity entity = new SkillEntity();
        entity.setSkillId(skillId);
        entity.setResourceId(skillId);
        entity.setSkillName(dto.getSkillName());
        entity.setOwnerId(userId);
        entity.setDescription(dto.getDescription());
        entity.setSourceType(dto.getSourceType());
        entity.setSkillStatus(SkillStatusEnum.DRAFT);
        entity.setAuditStatus(SkillAuditStatusEnum.NOT_SUBMITTED);
        skillRepository.save(entity);
        return skillId;
    }

    @Override
    @Transactional
    public void deleteSkills(List<String> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return;
        }
        skillRepository.deleteBySkillIdIn(skillIds);
    }

    @Override
    public void updateSkill(SkillUpdateReqDTO dto) {
        SkillEntity entity = skillRepository.findBySkillId(dto.getSkillId())
                .orElseThrow(() -> new ServiceException(SkillError.SKILL_NOT_FOUND));
        if (dto.getSkillName() != null) {
            entity.setSkillName(dto.getSkillName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        skillRepository.save(entity);
    }

    @Override
    public SkillInfoRespDTO getSkillInfo(String skillId) {
        SkillEntity entity = skillRepository.findBySkillId(skillId)
                .orElseThrow(() -> new ServiceException(SkillError.SKILL_NOT_FOUND));
        SkillInfoRespDTO response = BeanUtil.copyProperties(entity, SkillInfoRespDTO.class);
        if (entity.getCurrentVersionInfo() != null) {
            SkillVersionBase versionBase = new SkillVersionBase();
            versionBase.setVersion(entity.getCurrentVersionInfo().getVersion());
            versionBase.setSkillMdObjectKey(entity.getCurrentVersionInfo().getSkillMdObjectKey());
            versionBase.setPublished(entity.getCurrentVersionInfo().getPublished());
            versionBase.setEnabled(entity.getCurrentVersionInfo().getEnabled());
            for (SkillAssetEntity asset : entity.getCurrentVersionInfo().getAssetsManifest()) {
                versionBase.getAssetsManifest().add(BeanUtil.copyProperties(asset, SkillAssetMetaBase.class));
            }
            response.setCurrentVersionInfo(versionBase);
        }
        return response;
    }

    @Override
    public UploadInitRespDTO initManifestUpload(SkillManifestUploadInitReqDTO dto) {
        validateVersion(dto.getVersion());
        SkillEntity entity = skillRepository.findBySkillId(dto.getSkillId())
                .orElseThrow(() -> new ServiceException(SkillError.SKILL_NOT_FOUND));

        UploadInitRespDTO response = skillStorageService.initManifestUpload(
                dto.getSkillId(), dto.getVersion(), dto.getMd5(), dto.getExpectedSize()
        );

        SkillVersionEntity versionInfo = getOrCreateCurrentVersion(entity, dto.getVersion());
        versionInfo.setSkillMdObjectKey(response.getObjectKey());
        skillRepository.save(entity);
        return response;
    }

    @Override
    public UploadInitRespDTO initAssetUpload(SkillAssetUploadInitReqDTO dto) {
        validateVersion(dto.getVersion());
        validateRelativePath(dto.getRelativePath());
        SkillEntity entity = skillRepository.findBySkillId(dto.getSkillId())
                .orElseThrow(() -> new ServiceException(SkillError.SKILL_NOT_FOUND));

        UploadInitRespDTO response = skillStorageService.initAssetUpload(
                dto.getSkillId(), dto.getVersion(), dto.getRelativePath(), dto.getMd5(), dto.getExpectedSize()
        );

        SkillVersionEntity versionInfo = getOrCreateCurrentVersion(entity, dto.getVersion());
        SkillAssetEntity asset = findOrCreateAsset(versionInfo, dto.getRelativePath());
        asset.setObjectKey(response.getObjectKey());
        asset.setKind(dto.getKind());
        asset.setSizeBytes(dto.getExpectedSize());
        skillRepository.save(entity);
        return response;
    }

    private SkillVersionEntity getOrCreateCurrentVersion(SkillEntity entity, String version) {
        SkillVersionEntity versionInfo = entity.getCurrentVersionInfo();
        if (versionInfo == null || versionInfo.getVersion() == null || !version.equals(versionInfo.getVersion())) {
            versionInfo = new SkillVersionEntity();
            versionInfo.setVersion(version);
            entity.setCurrentVersionInfo(versionInfo);
        }
        return versionInfo;
    }

    private SkillAssetEntity findOrCreateAsset(SkillVersionEntity versionInfo, String relativePath) {
        for (SkillAssetEntity item : versionInfo.getAssetsManifest()) {
            if (relativePath.equals(item.getPath())) {
                return item;
            }
        }
        SkillAssetEntity asset = new SkillAssetEntity();
        asset.setPath(relativePath);
        versionInfo.getAssetsManifest().add(asset);
        return asset;
    }

    private void validateVersion(String version) {
        if (version == null || version.isBlank() || version.contains("/") || version.contains("\\")
                || ".".equals(version) || "..".equals(version)) {
            throw new ServiceException(SkillError.SKILL_VERSION_INVALID);
        }
    }

    private void validateRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank() || relativePath.startsWith("/")
                || relativePath.contains("\\") || List.of(relativePath.split("/")).contains("..")) {
            throw new ServiceException(SkillError.SKILL_RELATIVE_PATH_INVALID);
        }
    }
}
