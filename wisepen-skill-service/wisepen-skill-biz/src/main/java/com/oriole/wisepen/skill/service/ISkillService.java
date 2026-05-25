package com.oriole.wisepen.skill.service;

import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitRespDTO;
import com.oriole.wisepen.skill.domain.dto.SkillAssetUploadInitReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillCreateReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillInfoRespDTO;
import com.oriole.wisepen.skill.domain.dto.SkillManifestUploadInitReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillUpdateReqDTO;

import java.util.List;

public interface ISkillService {
    String createSkill(SkillCreateReqDTO dto, String userId);

    void deleteSkills(List<String> skillIds);

    void updateSkill(SkillUpdateReqDTO dto);

    SkillInfoRespDTO getSkillInfo(String skillId);

    UploadInitRespDTO initManifestUpload(SkillManifestUploadInitReqDTO dto);

    UploadInitRespDTO initAssetUpload(SkillAssetUploadInitReqDTO dto);
}
