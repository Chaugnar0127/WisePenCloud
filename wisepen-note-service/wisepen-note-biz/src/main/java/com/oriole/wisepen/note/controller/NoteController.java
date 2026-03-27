package com.oriole.wisepen.note.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.note.api.domain.dto.req.NoteCreateRequest;
import com.oriole.wisepen.note.api.domain.dto.res.NoteInfoResponse;
import com.oriole.wisepen.note.api.domain.dto.res.NoteOperationLogResponse;
import com.oriole.wisepen.note.api.domain.dto.res.NoteVersionListResponse;
import com.oriole.wisepen.note.config.NoteProperties;
import com.oriole.wisepen.note.service.INoteOperationLogService;
import com.oriole.wisepen.note.service.INoteService;
import com.oriole.wisepen.note.service.INoteVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "笔记服务", description = "笔记的创建、删除、版本管理与操作日志")
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
@CheckLogin
public class NoteController {

    private final INoteService noteService;
    private final INoteVersionService noteVersionService;
    private final INoteOperationLogService noteOperationLogService;
    private final NoteProperties noteProperties;

    @Operation(summary = "创建笔记")
    @PostMapping("/create")
    public R<String> createNote(@Validated @RequestBody NoteCreateRequest request) {
        String userId = SecurityContextHolder.getUserId().toString();
        String resourceId = noteService.createNote(request, userId);
        return R.ok(resourceId);
    }

    @Operation(summary = "删除笔记")
    @DeleteMapping("/{resourceId}")
    public R<Void> deleteNote(@PathVariable String resourceId) {
        String userId = SecurityContextHolder.getUserId().toString();
        noteService.deleteNote(resourceId, userId);
        return R.ok();
    }

    @Operation(summary = "获取笔记信息")
    @GetMapping("/{resourceId}")
    public R<NoteInfoResponse> getNoteInfo(@PathVariable String resourceId) {
        return R.ok(noteService.getNoteInfo(resourceId));
    }

    @Operation(summary = "查询版本历史列表")
    @GetMapping("/{resourceId}/versions")
    public R<Page<NoteVersionListResponse>> listVersions(
            @PathVariable String resourceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(noteVersionService.listVersions(resourceId, page, size));
    }

    @Operation(summary = "手动保存当前版本")
    @PostMapping("/{resourceId}/versions/save")
    public R<Void> saveVersion(
            @PathVariable String resourceId,
            @RequestParam(required = false) String label) {
        String userId = SecurityContextHolder.getUserId().toString();
        noteVersionService.saveManualVersion(resourceId, label, userId);
        return R.ok();
    }

    @Operation(summary = "回退到指定版本")
    @PostMapping("/{resourceId}/versions/{version}/revert")
    public R<Void> revertToVersion(
            @PathVariable String resourceId,
            @PathVariable Long version) {
        // 获取 FULL + DELTA 链，实际 Yjs 重建由 Node.js 完成
        // 此处需通过 HTTP 调用 Node.js 的 /internal-collab/revert 接口
        // TODO: 实现与 Node.js 协同服务的 revert 交互
        return R.ok();
    }

    @Operation(summary = "查询操作日志")
    @GetMapping("/{resourceId}/oplog")
    public R<Page<NoteOperationLogResponse>> listOperationLogs(
            @PathVariable String resourceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(noteOperationLogService.listOperationLogs(resourceId, page, size));
    }

    @Operation(summary = "获取操作日志颗粒度配置")
    @GetMapping("/config/oplog-granularity")
    public R<String> getOplogGranularity() {
        return R.ok(noteProperties.getOplogGranularity());
    }
}
