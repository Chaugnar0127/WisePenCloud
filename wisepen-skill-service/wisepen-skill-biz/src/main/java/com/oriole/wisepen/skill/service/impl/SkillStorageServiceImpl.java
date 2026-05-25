package com.oriole.wisepen.skill.service.impl;

import com.oriole.wisepen.common.core.domain.IResult;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitReqDTO;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitRespDTO;
import com.oriole.wisepen.file.storage.api.enums.StorageSceneEnum;
import com.oriole.wisepen.file.storage.api.feign.RemoteStorageService;
import com.oriole.wisepen.skill.exception.SkillError;
import com.oriole.wisepen.skill.service.ISkillStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillStorageServiceImpl implements ISkillStorageService {

    private final RemoteStorageService remoteStorageService;

    @Override
    public UploadInitRespDTO initManifestUpload(String skillId, String version, String md5, Long expectedSize) {
        return initUpload(buildObjectKey(skillId, version, "SKILL.md"), "md", md5, expectedSize);
    }

    @Override
    public UploadInitRespDTO initAssetUpload(String skillId, String version, String relativePath, String md5, Long expectedSize) {
        return initUpload(buildObjectKey(skillId, version, relativePath), extractExtension(relativePath), md5, expectedSize);
    }

    private UploadInitRespDTO initUpload(String objectKey, String extension, String md5, Long expectedSize) {
        return unwrap(remoteStorageService.initUpload(UploadInitReqDTO.builder()
                .md5(md5)
                .extension(extension)
                .scene(StorageSceneEnum.PRIVATE_DOC)
                .objectKey(objectKey)
                .expectedSize(expectedSize)
                .build()), SkillError.SKILL_UPLOAD_INIT_FAILED);
    }

    private String buildObjectKey(String skillId, String version, String relativePath) {
        return "skills/" + skillId + "/" + version + "/" + relativePath;
    }

    private String extractExtension(String relativePath) {
        int dotIndex = relativePath.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == relativePath.length() - 1) {
            return "bin";
        }
        return relativePath.substring(dotIndex + 1);
    }

    private <T> T unwrap(R<T> response, IResult message) {
        if (response == null) {
            throw new ServiceException(message);
        }
        if (response.getCode() == null || response.getCode() != 200) {
            throw new ServiceException(message, response.getMsg());
        }
        if (response.getData() == null) {
            throw new ServiceException(message);
        }
        return response.getData();
    }

}
