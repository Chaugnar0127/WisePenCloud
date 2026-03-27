package com.oriole.wisepen.note.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import com.oriole.wisepen.note.api.feign.RemoteNoteService;
import com.oriole.wisepen.note.config.NoteProperties;
import com.oriole.wisepen.note.service.INoteService;
import com.oriole.wisepen.note.service.INoteVersionService;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

@Tag(name = "内部笔记服务", description = "供 Node.js 协同服务和其他微服务调用的内部接口")
@RestController
@RequestMapping("/internal/note")
@RequiredArgsConstructor
public class InternalNoteController implements RemoteNoteService {

    private final INoteService noteService;
    private final INoteVersionService noteVersionService;
    private final NoteProperties noteProperties;

    @Operation(summary = "检查资源权限")
    @PostMapping("/checkPermission")
    @Override
    public R<ResourceCheckPermissionResDTO> checkPermission(
            @Validated @RequestBody ResourceCheckPermissionReqDTO dto) {
        return R.ok(noteService.checkPermission(dto));
    }

    @Operation(summary = "获取最新快照")
    @GetMapping("/snapshot/{resourceId}")
    @Override
    public R<NoteSnapshotResponse> getLatestSnapshot(@PathVariable("resourceId") String resourceId) {
        return R.ok(noteService.getLatestSnapshot(resourceId));
    }

    @Operation(summary = "获取操作日志颗粒度配置")
    @GetMapping("/config/oplog-granularity")
    public R<String> getOplogGranularity() {
        return R.ok(noteProperties.getOplogGranularity());
    }

    @Operation(summary = "获取版本链用于回退重建")
    @GetMapping("/versionChain/{resourceId}/{version}")
    public R<List<String>> getVersionChain(
            @PathVariable String resourceId,
            @PathVariable Long version) {
        byte[][] chain = noteVersionService.getVersionChainForRevert(resourceId, version);
        List<String> base64Chain = java.util.Arrays.stream(chain)
                .map(b -> Base64.getEncoder().encodeToString(b))
                .toList();
        return R.ok(base64Chain);
    }
}
