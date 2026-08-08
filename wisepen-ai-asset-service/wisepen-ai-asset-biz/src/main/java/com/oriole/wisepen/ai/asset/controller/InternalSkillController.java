package com.oriole.wisepen.ai.asset.controller;

import cn.hutool.core.bean.BeanUtil;
import com.oriole.wisepen.ai.asset.domain.base.AIResourceInfoBase;
import com.oriole.wisepen.ai.asset.domain.dto.req.AIResourceMetaInfoListRequest;
import com.oriole.wisepen.ai.asset.domain.dto.res.SkillInfoResponse;
import com.oriole.wisepen.ai.asset.domain.dto.res.AIResourceMetaInfoResponse;
import com.oriole.wisepen.ai.asset.domain.dto.res.SkillVersionBundleInfoResponse;
import com.oriole.wisepen.ai.asset.domain.entity.SkillVersionBundleEntity;
import com.oriole.wisepen.ai.asset.exception.AIResourceError;
import com.oriole.wisepen.ai.asset.service.impl.SkillServiceImpl;
import com.oriole.wisepen.ai.asset.service.impl.SkillVersionServiceImpl;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "内部 - 技能资产", description = "供内部服务按资源 ID 查询技能资产主档、版本快照和已发布元信息")
@RestController
@RequestMapping("/internal/skill")
@RequiredArgsConstructor
public class InternalSkillController {

    private final SkillServiceImpl skillService;
    private final SkillVersionServiceImpl skillVersionService;

    @Operation(
            summary = "内部获取技能资产",
            description = """
                    - 用途：供内部服务按资源 ID 补全技能资产主档和指定版本快照。
                    - 请求：resourceId 指定技能资产资源；skillVersion 可选，未传时使用技能主档当前发布版本。
                    - 约束：调用方必须通过内部服务调用边界；技能主档必须存在；目标版本必须大于 0 且存在。
                    - 处理：读取技能主档并补充业务更新时间，再读取目标版本包信息；不做用户权限判断，不修改资产或版本。
                    - 失败：技能不存在 -> AIResourceError.AI_RESOURCE_NOT_FOUND；版本号小于等于 0 或目标版本不存在 -> AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND。
                    - 响应：返回技能主档信息、业务更新时间和目标版本包信息。
                    """
    )
    @GetMapping("/getSkillByResourceId")
    public R<SkillInfoResponse> getSkillByResourceId(@RequestParam String resourceId, @RequestParam(required = false) Integer skillVersion) {
        AIResourceInfoBase skill = skillService.getAIResourceInfo(resourceId);
        if (skillVersion == null) skillVersion = skill.getVersion();
        if (skillVersion <= 0) {
            throw new ServiceException(AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND);
        }
        SkillInfoResponse response = BeanUtil.copyProperties(skill, SkillInfoResponse.class);
        SkillVersionBundleEntity bundle = skillVersionService.getVersionBundle(resourceId, skillVersion);
        response.setSkillVersionBundle(BeanUtil.copyProperties(bundle, SkillVersionBundleInfoResponse.class));
        return R.ok(response);
    }

    @Operation(
            summary = "内部批量获取已发布技能元信息",
            description = """
                    - 用途：供内部服务按资源 ID 批量补全已发布技能资产的主档元信息。
                    - 请求：请求体携带 resourceIds；为空或 null 时按空结果处理。
                    - 约束：调用方必须通过内部服务调用边界；只返回已发布版本号大于 0 的技能主档。
                    - 处理：批量查询技能主档，过滤未发布技能，复制主档元信息和业务更新时间；不读取版本包文件，不做用户权限判断。
                    - 失败：底层存储查询发生未处理异常 -> CommonError.INTERNAL_ERROR。
                    - 响应：返回已发布技能主档元信息列表，每项包含业务更新时间。
                    """
    )
    @PostMapping("/listPublishedSkillsMetaByResourceIds")
    public R<List<AIResourceMetaInfoResponse>> listPublishedSkillMetasByResourceIds(@RequestBody AIResourceMetaInfoListRequest request) {
        return R.ok(skillService.listPublishedAIResourcesMeta(request == null ? null : request.getResourceIds()));
    }
}
