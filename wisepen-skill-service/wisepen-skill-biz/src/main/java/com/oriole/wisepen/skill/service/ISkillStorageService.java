package com.oriole.wisepen.skill.service;

import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitRespDTO;

import java.util.List;

public interface ISkillStorageService {
    UploadInitRespDTO initManifestUpload(String skillId, String version, String md5, Long expectedSize);

    UploadInitRespDTO initAssetUpload(String skillId, String version, String relativePath, String md5, Long expectedSize);

}
