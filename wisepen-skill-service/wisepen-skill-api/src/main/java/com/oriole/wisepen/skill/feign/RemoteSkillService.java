package com.oriole.wisepen.skill.feign;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitRespDTO;
import com.oriole.wisepen.skill.domain.dto.SkillAssetUploadInitReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillCreateReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillInfoGetReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillInfoRespDTO;
import com.oriole.wisepen.skill.domain.dto.SkillManifestUploadInitReqDTO;
import com.oriole.wisepen.skill.domain.dto.SkillUpdateReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "内部 Skill 服务", description = "提供给其他微服务的 Skill Feign 接口")
@FeignClient(contextId = "remoteSkillService", value = "wisepen-skill-service")
public interface RemoteSkillService {

    @Operation(summary = "创建 Skill", description = "注册一个 Skill 主档并返回 Skill ID")
    @PostMapping("/internal/skill/createSkill")
    R<String> createSkill(@RequestBody SkillCreateReqDTO dto);

    @Operation(summary = "更新 Skill", description = "更新 Skill 的名称与描述等基础元信息")
    @PostMapping("/internal/skill/changeSkill")
    R<Void> updateSkill(@RequestBody SkillUpdateReqDTO dto);

    @Operation(summary = "获取 Skill 详情", description = "查询 Skill 主档信息与主要路径")
    @PostMapping("/internal/skill/getSkillInfo")
    R<SkillInfoRespDTO> getSkillInfo(@RequestBody SkillInfoGetReqDTO dto);

    @Operation(summary = "初始化 SKILL.md 上传", description = "为指定 Skill 版本初始化 SKILL.md 上传")
    @PostMapping("/internal/skill/initManifestUpload")
    R<UploadInitRespDTO> initManifestUpload(@RequestBody SkillManifestUploadInitReqDTO dto);

    @Operation(summary = "初始化 Skill 资产上传", description = "为指定 Skill 版本初始化资产上传")
    @PostMapping("/internal/skill/initAssetUpload")
    R<UploadInitRespDTO> initAssetUpload(@RequestBody SkillAssetUploadInitReqDTO dto);
}
