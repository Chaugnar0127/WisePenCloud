package com.oriole.wisepen.skill.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitRespDTO;
import com.oriole.wisepen.skill.domain.dto.SkillAssetUploadInitReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillCreateReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillInfoGetReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillInfoRespDTO;
import com.oriole.wisepen.skill.domain.dto.SkillManifestUploadInitReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillUpdateReqDTO;
import com.oriole.wisepen.skill.feign.RemoteSkillService;
import com.oriole.wisepen.skill.service.ISkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/skill")
@RequiredArgsConstructor
public class InternalSkillController implements RemoteSkillService {

    private final ISkillService skillService;

    @Override
    @PostMapping("/createSkill")
    public R<String> createSkill(@Validated @RequestBody SkillCreateReqDTO dto) {
        return R.ok(skillService.createSkill(dto, dto.getOwnerId()));
    }

    @Override
    @PostMapping("/changeSkill")
    public R<Void> updateSkill(@Validated @RequestBody SkillUpdateReqDTO dto) {
        skillService.updateSkill(dto);
        return R.ok();
    }

    @Override
    @PostMapping("/getSkillInfo")
    public R<SkillInfoRespDTO> getSkillInfo(@Validated @RequestBody SkillInfoGetReqDTO dto) {
        return R.ok(skillService.getSkillInfo(dto.getSkillId()));
    }

    @Override
    @PostMapping("/initManifestUpload")
    public R<UploadInitRespDTO> initManifestUpload(@Validated @RequestBody SkillManifestUploadInitReqDTO dto) {
        return R.ok(skillService.initManifestUpload(dto));
    }

    @Override
    @PostMapping("/initAssetUpload")
    public R<UploadInitRespDTO> initAssetUpload(@Validated @RequestBody SkillAssetUploadInitReqDTO dto) {
        return R.ok(skillService.initAssetUpload(dto));
    }
}
