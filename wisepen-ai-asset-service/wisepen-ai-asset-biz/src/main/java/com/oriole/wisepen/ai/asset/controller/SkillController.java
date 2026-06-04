package com.oriole.wisepen.ai.asset.controller;

import com.oriole.wisepen.ai.asset.domain.base.SkillInfoBase;
import com.oriole.wisepen.ai.asset.domain.dto.req.SkillAssetDeleteRequest;
import com.oriole.wisepen.ai.asset.domain.dto.req.SkillAssetUploadInitRequest;
import com.oriole.wisepen.ai.asset.domain.dto.req.SkillCreateRequest;
import com.oriole.wisepen.ai.asset.domain.dto.req.SkillUpdateRequest;
import com.oriole.wisepen.ai.asset.domain.dto.req.SkillVersionPublishRequest;
import com.oriole.wisepen.ai.asset.domain.dto.res.SkillAssetUploadInitResponse;
import com.oriole.wisepen.ai.asset.domain.dto.res.SkillInfoResponse;
import com.oriole.wisepen.ai.asset.domain.dto.res.SkillVersionInfoResponse;
import com.oriole.wisepen.ai.asset.exception.SkillError;
import com.oriole.wisepen.ai.asset.service.ISkillService;
import com.oriole.wisepen.ai.asset.service.ISkillVersionService;
import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceInfoGetReqDTO;
import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import com.oriole.wisepen.resource.enums.ResourceAccessRole;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Skill 管理", description = "Skill 的创建、更新与查询")
@RestController
@RequestMapping("/skill")
@RequiredArgsConstructor
@CheckLogin
public class SkillController {

    private final ISkillService skillService;
    private final ISkillVersionService skillVersionService;
    private final RemoteResourceService remoteResourceService;

    @Operation(summary = "创建 Skill")
    @Log(title = "创建 Skill", businessType = BusinessType.INSERT)
    @PostMapping("/createSkill")
    public R<String> createSkill(@Validated @RequestBody SkillCreateRequest request) {
        String userId = SecurityContextHolder.getUserId().toString();
        String resourceId = skillService.createSkill(request, userId);
        return R.ok(resourceId);
    }

    @Operation(summary = "更新 Skill 信息", description = "更新 Skill 的name与desc")
    @Log(title = "更新 Skill 信息", businessType = BusinessType.UPDATE)
    @PostMapping("/changeSkillInfo")
    public R<Void> updateSkillInfo(@Validated @RequestBody SkillUpdateRequest request) {
        assertSkillOwner(request.getResourceId());
        skillService.updateSkill(request);
        return R.ok();
    }

    @Operation(summary = "获取 Skill 信息")
    @PostMapping("/getSkillInfo")
    public R<SkillInfoResponse> getSkillInfo(@RequestParam String resourceId) {
        // 若无权限将抛出异常，此处无需重复鉴权
        ResourceItemResponse resourceInfo = remoteResourceService.getResourceInfo(new ResourceInfoGetReqDTO(
                resourceId, SecurityContextHolder.getUserId(), SecurityContextHolder.getGroupRoleMap()
        )).getData();
        SkillInfoBase skillInfo = skillService.getSkillInfo(resourceId);
        SkillInfoResponse skillInfoResponse = SkillInfoResponse.builder().resourceInfo(resourceInfo).skillInfo(skillInfo).build();
        return R.ok(skillInfoResponse);
    }

    @Operation(summary = "获取 Skill 特定版本信息", description = "查询指定版本或当前确认版本的文件快照")
    @PostMapping("/getSkillVersionInfo")
    public R<SkillVersionInfoResponse> getSkillVersionInfo(@RequestParam String resourceId, Integer version) {
        assertSkillOwner(resourceId);
        return R.ok(skillVersionService.getSkillVersion(resourceId, version));
    }

    @Operation(summary = "发布 Skill 版本", description = "确认当前草稿版本并推进主档版本号")
    @Log(title = "发布 Skill 版本", businessType = BusinessType.UPDATE)
    @PostMapping("/publishSkillVersion")
    public R<Void> publishSkillVersion(@Validated @RequestBody SkillVersionPublishRequest request) {
        assertSkillOwner(request.getResourceId());
        skillVersionService.publishSkillVersion(request);
        return R.ok();
    }

    @Operation(summary = "上传 Skill 资源", description = "为草稿版本Skill添加或替换文件")
    @Log(title = "上传 Skill 资源", businessType = BusinessType.INSERT)
    @PostMapping("/initUploadSkillAssets")
    public R<SkillAssetUploadInitResponse> initUploadSkillAssets(@Validated @RequestBody SkillAssetUploadInitRequest request) {
        assertSkillOwner(request.getResourceId());
        SkillAssetUploadInitResponse skillAssetUploadInitResponse = skillVersionService.initUploadSkillAssets(request);
        return R.ok(skillAssetUploadInitResponse);
    }

    @Operation(summary = "删除草稿中的 Skill 资源", description = "为草稿版本Skill删除文件")
    @Log(title = "Delete Skill asset", businessType = BusinessType.DELETE)
    @PostMapping("/deleteSkillAssets")
    public R<Void> deleteSkillAssets(@Validated @RequestBody SkillAssetDeleteRequest request) {
        assertSkillOwner(request.getResourceId());
        skillVersionService.deleteSkillAssets(request);
        return R.ok();
    }

    private void assertSkillOwner(String resourceId) {
        ResourceCheckPermissionResDTO permission = remoteResourceService.checkResPermission(ResourceCheckPermissionReqDTO.builder()
                .resourceId(resourceId)
                .userId(SecurityContextHolder.getUserId())
                .groupRoles(SecurityContextHolder.getGroupRoleMap())
                .build()).getData();
        if (permission == null || permission.getResourceAccessRole() != ResourceAccessRole.OWNER) {
            throw new ServiceException(SkillError.SKILL_OWNER_MISMATCH);
        }
    }
}
