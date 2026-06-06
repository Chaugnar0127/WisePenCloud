package com.oriole.wisepen.note.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import com.oriole.wisepen.note.api.feign.RemoteNoteService;
import com.oriole.wisepen.note.service.INoteVersionService;
import com.oriole.wisepen.resource.domain.dto.req.ResourceForkRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@Tag(name = "内部笔记服务", description = "供 Node.js 协同服务和其他微服务调用的内部接口")
@RestController
@RequestMapping("/internal/note")
@RequiredArgsConstructor
public class InternalNoteController implements RemoteNoteService {

    private final INoteVersionService noteVersionService;

    @Operation(summary = "获取最新快照")
    @GetMapping("/getNoteLatestVersion")
    @Override
    public R<NoteSnapshotResponse> getNoteLatestVersion(@RequestParam("resourceId") String resourceId) {
        return R.ok(noteVersionService.getLatestVersion(resourceId));
    }

    @Operation(summary = "获取指定版本快照")
    @GetMapping("/getNoteSnapshot")
    @Override
    public R<NoteSnapshotResponse> getNoteSnapshot(@RequestParam("resourceId") String resourceId,
                                                   @RequestParam("version") Long version) {
        return R.ok(noteVersionService.getSnapshot(resourceId, version));
    }

    @Operation(summary = "复制指定版本笔记")
    @PostMapping("/forkNote")
    @Override
    public R<Void> forkNote(@Valid @RequestBody ResourceForkRequest request) {
        noteVersionService.forkNote(request);
        return R.ok();
    }
}
