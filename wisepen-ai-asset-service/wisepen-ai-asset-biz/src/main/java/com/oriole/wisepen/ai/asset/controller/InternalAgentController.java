package com.oriole.wisepen.ai.asset.controller;

import cn.hutool.core.bean.BeanUtil;
import com.oriole.wisepen.ai.asset.domain.base.AIResourceInfoBase;
import com.oriole.wisepen.ai.asset.domain.dto.res.AgentInfoResponse;
import com.oriole.wisepen.ai.asset.domain.dto.res.AgentVersionBundleInfoResponse;
import com.oriole.wisepen.ai.asset.domain.entity.AgentVersionBundleEntity;
import com.oriole.wisepen.ai.asset.exception.AIResourceError;
import com.oriole.wisepen.ai.asset.service.impl.AgentServiceImpl;
import com.oriole.wisepen.ai.asset.service.impl.AgentVersionServiceImpl;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "内部 - 智能体资产", description = "供内部服务按资源 ID 查询智能体资产主档和版本快照")
@RestController
@RequestMapping("/internal/agent")
@RequiredArgsConstructor
public class InternalAgentController {

    private final AgentServiceImpl agentService;
    private final AgentVersionServiceImpl agentVersionService;


    @Operation(
            summary = "内部获取智能体资产",
            description = """
                    - 用途：供内部服务按资源 ID 补全智能体资产主档和指定版本快照。
                    - 请求：resourceId 指定智能体资产资源；agentVersion 可选，未传时使用智能体主档当前发布版本。
                    - 约束：调用方必须通过内部服务调用边界；智能体主档必须存在；目标版本必须大于 0 且存在。
                    - 处理：读取智能体主档并补充业务更新时间，再读取目标版本包信息；不做用户权限判断，不修改资产或版本。
                    - 失败：智能体不存在 -> AIResourceError.AI_RESOURCE_NOT_FOUND；版本号小于等于 0 或目标版本不存在 -> AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND。
                    - 响应：返回智能体主档信息、业务更新时间和目标版本包信息。
                    """
    )
    @GetMapping("/getAgentByResourceId")
    public R<AgentInfoResponse> getAgentByResourceId(@RequestParam String resourceId, @RequestParam(required = false) Integer agentVersion) {
        AIResourceInfoBase agent = agentService.getAIResourceInfo(resourceId);
        if (agentVersion == null) agentVersion = agent.getVersion();
        if (agentVersion <= 0) {
            throw new ServiceException(AIResourceError.AI_RESOURCE_VERSION_NOT_FOUND);
        }
        AgentInfoResponse response = BeanUtil.copyProperties(agent, AgentInfoResponse.class);
        AgentVersionBundleEntity bundle = agentVersionService.getVersionBundle(resourceId, agentVersion);
        response.setAgentVersionBundle(BeanUtil.copyProperties(bundle, AgentVersionBundleInfoResponse.class));
        return R.ok(response);
    }
}