package com.oriole.wisepen.note.api.feign;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "内部笔记服务", description = "提供给 Node.js 协同服务和其他微服务的 Feign 接口")
@FeignClient(contextId = "remoteNoteService", value = "wisepen-note-service")
public interface RemoteNoteService {

    @Operation(summary = "获取最新快照", description = "获取指定笔记的最新完整 Yjs 快照")
    @GetMapping("/internal/note/getNoteLatestVersion")
    R<NoteSnapshotResponse> getNoteLatestVersion(@RequestParam("resourceId") String resourceId);
}
