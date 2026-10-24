package com.oriole.wisepen.system.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckRole;
import com.oriole.wisepen.system.api.domain.dto.req.BatchTaskCreateRequest;
import com.oriole.wisepen.system.api.domain.dto.res.BatchTaskItemResponse;
import com.oriole.wisepen.system.api.domain.dto.res.BatchTaskResponse;
import com.oriole.wisepen.system.api.enums.BatchTaskItemStatus;
import com.oriole.wisepen.system.service.BatchTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员 - 批量任务", description = "管理员创建、发起和查询通用批量任务")
@RestController
@RequestMapping("/system/admin/task")
@RequiredArgsConstructor
@CheckRole(IdentityType.ADMIN)
@Validated
public class AdminBatchTaskController {

    private final BatchTaskService batchTaskService;

    @Operation(
            summary = "创建批量任务",
            description = """
                    - 用途：创建由 type 选择执行器、meta 表达业务参数的通用批量任务，并生成独立任务项。
                    - 请求：requestId 是任务类型内的幂等号；type 指定任务执行器；name 是管理端展示名称；meta 由对应执行器校验。RESOURCE_FORK 首版接收一个 NOTE 或 DRAWIO 源资源、固定版本、目标用户列表和统一文件夹路径。
                    - 约束：当前身份必须是平台管理员；type 必须存在对应执行器；requestId 与 type 组合唯一；meta 必须满足执行器契约；单任务项数不能超过服务配置上限。
                    - 处理：执行器校验并规范化 meta、批量校验目标用户、拆分任务项，在一个本地事务中保存 CREATED 任务；相同 type 和 requestId 重复请求返回原任务；本接口不发起执行。
                    - 失败：任务类型无执行器 -> SysError.BATCH_TASK_TYPE_UNSUPPORTED；meta 非法 -> SysError.BATCH_TASK_META_INVALID；源资源不存在 -> SysError.RESOURCE_FORK_SOURCE_NOT_FOUND；源资源类型暂不支持 -> SysError.RESOURCE_FORK_TYPE_UNSUPPORTED。
                    - 响应：返回任务信息、规范化后的 meta 和按任务项状态汇总的数量。
                    """
    )
    @Log(title = "创建批量任务", businessType = BusinessType.INSERT,
            isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping("/createTask")
    public R<BatchTaskResponse> createTask(@Valid @RequestBody BatchTaskCreateRequest request) {
        return R.ok(batchTaskService.createTask(request, SecurityContextHolder.getUserId()));
    }

    @Operation(
            summary = "发起批量任务",
            description = """
                    - 用途：将已经创建并确认的批量任务交给后台执行器异步处理。
                    - 请求：taskId 指定目标任务。
                    - 约束：当前身份必须是平台管理员；任务必须存在且处于 CREATED 状态；重复发起 PENDING 或 RUNNING 任务按幂等成功处理。
                    - 处理：存在可执行任务项时把任务推进为 PENDING，由后台 Worker 逐项尽力执行；全部任务项在创建阶段已失败时直接将任务置为 FAILED；不等待业务任务执行完成。
                    - 失败：任务不存在 -> SysError.BATCH_TASK_NOT_FOUND；任务已处于终态 -> SysError.BATCH_TASK_STATE_INVALID。
                    - 响应：成功时返回空结果。
                    """
    )
    @Log(title = "发起批量任务", businessType = BusinessType.UPDATE)
    @PostMapping("/startTask")
    public R<Void> startTask(@RequestParam Long taskId) {
        batchTaskService.startTask(taskId);
        return R.ok();
    }

    @Operation(
            summary = "查询批量任务",
            description = """
                    - 用途：查询批量任务的当前生命周期状态、规范化业务参数和执行进度。
                    - 请求：taskId 指定目标任务。
                    - 约束：当前身份必须是平台管理员；任务必须存在。
                    - 处理：读取任务主记录，并按任务项状态实时聚合待执行、执行中、成功和失败数量；不修改任务状态。
                    - 失败：任务不存在 -> SysError.BATCH_TASK_NOT_FOUND。
                    - 响应：返回任务信息、meta、各状态数量和生命周期时间。
                    """
    )
    @GetMapping("/getTaskInfo")
    public R<BatchTaskResponse> getTaskInfo(@RequestParam Long taskId) {
        return R.ok(batchTaskService.getTaskInfo(taskId));
    }

    @Operation(
            summary = "分页查询批量任务项",
            description = """
                    - 用途：分页查看批量任务中每个业务目标的执行状态、结果或失败原因。
                    - 请求：taskId 指定任务；status 可选，用于筛选任务项状态；page 和 size 控制分页，size 最大为 100。
                    - 约束：当前身份必须是平台管理员；任务必须存在。
                    - 处理：按任务项 ID 升序分页读取，解析通用 meta 和 result；不重试或修改任务项。
                    - 失败：任务不存在 -> SysError.BATCH_TASK_NOT_FOUND。
                    - 响应：返回任务项分页、状态、结构化参数与结果、错误码和生命周期时间。
                    """
    )
    @GetMapping("/listTaskItems")
    public R<PageR<BatchTaskItemResponse>> listTaskItems(
            @RequestParam Long taskId,
            @RequestParam(required = false) BatchTaskItemStatus status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return R.ok(batchTaskService.listTaskItems(taskId, status, page, size));
    }
}
