package com.oriole.wisepen.resource.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCreateReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceUpdateReqDTO;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import com.oriole.wisepen.resource.service.IGroupResService;
import com.oriole.wisepen.resource.service.IResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/resource")
@RequiredArgsConstructor
public class InternalResourceItemController implements RemoteResourceService {

    private final IResourceService resourceService;
    private final IGroupResService groupResService;

    // 注册/新增资源摘要
    @PostMapping("/addRes")
    public R<String> createResource(@Validated @RequestBody ResourceCreateReqDTO dto) {
        String resourceId = resourceService.createResourceItem(dto);
        return R.ok(resourceId);
    }

    // 删除资源摘要
    @PostMapping("/deleteRes")
    public R<Void> removeResource(@RequestParam("resourceId") String resourceId) {
        resourceService.softRemoveResourceItem(resourceId);
        return R.ok();
    }

    // 同步修改资源属性

    @PostMapping("/changeResAttr")
    public R<Void> updateAttributes(@Validated @RequestBody ResourceUpdateReqDTO dto) {
        resourceService.updateResourceAttributes(dto);
        return R.ok();
    }

    // 内部鉴权接口，供下游微服务在执行敏感操作（如：导出PDF、分享链接）前进行硬核鉴权
    @PostMapping("/checkResPermission")
    public R<ResourceCheckPermissionResDTO> checkResPermission(@Validated @RequestBody ResourceCheckPermissionReqDTO dto) {
        ResourceCheckPermissionResDTO hasPermission = resourceService.checkPermission(dto);
        return R.ok(hasPermission);
    }

    // 小组解散：软删除 Tag 树与配置，写入解散队列（30 天后由定时任务硬删）
    @PostMapping("/dissolveGroup")
    public R<Void> dissolveGroup(@RequestParam("groupId") Long groupId) {
        groupResService.softDissolveGroup(groupId.toString());
        return R.ok();
    }

}
