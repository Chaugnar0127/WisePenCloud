package com.oriole.wisepen.note.api.feign;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "内部笔记服务", description = "提供给 Node.js 协同服务和其他微服务的 Feign 接口")
@FeignClient(contextId = "remoteNoteService", value = "wisepen-note-service")
public interface RemoteNoteService {

    @Operation(summary = "检查资源权限", description = "校验用户对某笔记资源是否有访问权限")
    @PostMapping("/internal/note/checkPermission")
    R<ResourceCheckPermissionResDTO> checkPermission(@RequestBody ResourceCheckPermissionReqDTO dto);

    @Operation(summary = "获取最新快照", description = "获取指定笔记的最新完整 Yjs 快照")
    @GetMapping("/internal/note/snapshot/{resourceId}")
    R<NoteSnapshotResponse> getLatestSnapshot(@PathVariable("resourceId") String resourceId);
}
