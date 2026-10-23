package com.oriole.wisepen.note.controller;

import cn.hutool.core.bean.BeanUtil;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.note.api.domain.dto.NoteBatchForkReqDTO;
import com.oriole.wisepen.note.api.domain.dto.req.NoteForkRequest;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSearchTextResponse;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import com.oriole.wisepen.note.api.feign.RemoteNoteService;
import com.oriole.wisepen.note.service.INoteService;
import com.oriole.wisepen.note.service.INoteVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Tag(name = "内部 - 笔记", description = "供协同服务和业务微服务读取笔记快照、创建独立副本")
@RestController
@RequestMapping("/internal/note")
@RequiredArgsConstructor
public class InternalNoteController implements RemoteNoteService {

    private final INoteVersionService noteVersionService;
    private final INoteService noteService;

    @Operation(
            summary = "内部获取最新笔记快照",
            description = """
                    - 用途：供协同服务拉取笔记最新完整快照和后续增量版本，用于恢复协同编辑状态。
                    - 请求：resourceId 指定笔记资源。
                    - 约束：调用方必须通过内部服务调用边界；目标笔记必须存在。
                    - 处理：读取最近一个 FULL 快照，并追加其后的 DELTA 版本；如果没有 FULL 快照则返回空 fullSnapshot 和当前版本 0；不生成新版本。
                    - 失败：笔记不存在 -> NoteError.NOTE_NOT_FOUND；版本读取发生未处理异常 -> CommonError.INTERNAL_ERROR。
                    - 响应：返回资源 ID、最新版本号、完整快照和增量快照列表。
                    """
    )
    @GetMapping("/getNoteLatestVersion")
    @Override
    public R<NoteSnapshotResponse> getNoteLatestVersion(@RequestParam("resourceId") String resourceId) {
        return R.ok(noteVersionService.getLatestVersion(resourceId));
    }

    @GetMapping("/getNoteSearchText")
    @Override
    public R<NoteSearchTextResponse> getNoteSearchText(@RequestParam("resourceId") String resourceId,
                                                       @RequestParam(value = "version", required = false) Integer version) {
        return R.ok(noteVersionService.getSearchText(resourceId, version));
    }

    @Operation(
            summary = "内部复制笔记资源",
            description = """
                    - 用途：供系统为目标用户创建指定版本笔记的独立副本。
                    - 请求：sourceResourceId 和 sourceVersion 指定源快照；targetOwnerUserId、targetResourceName 和可选 targetTagId 指定副本归属、名称与个人云盘位置。
                    - 约束：调用方必须通过内部服务调用边界；源笔记及目标版本必须存在；目标标签必须属于目标用户个人空间。
                    - 处理：复用笔记 fork 流程复制源版本快照，注册目标用户拥有的新资源并挂载到指定个人文件夹；不继承源资源的小组、市场或互动数据。
                    - 失败：源笔记不存在 -> NoteError.NOTE_NOT_FOUND；资源注册失败 -> NoteError.NOTE_REGISTER_RESOURCE_FAILED；复制失败 -> NoteError.NOTE_FORK_FAILED。
                    - 响应：返回新副本的资源 ID。
                    """
    )
    @PostMapping("/forkNote")
    @Override
    public R<String> forkNote(@Validated @RequestBody NoteBatchForkReqDTO dto) {
        return R.ok(noteService.forkNote(BeanUtil.copyProperties(dto, NoteForkRequest.class), dto.getTargetOwnerUserId()));
    }
}
