package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.user.api.domain.dto.res.UserTourStatusResponse;
import com.oriole.wisepen.user.api.enums.TourCode;
import com.oriole.wisepen.user.service.IUserTourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户引导", description = "用户各项引导的完成状态与完成汇报")
@RestController
@RequestMapping("/user/tour")
@RequiredArgsConstructor
public class UserTourController {
    private final IUserTourService userTourService;

    @Operation(
            summary = "查询全部引导状态",
            description = """
                    - 用途：展示当前用户全部引导进度。
                    - 请求：无需请求参数，目标用户来自当前认证上下文。
                    - 约束：当前用户必须已登录；仅操作当前用户的引导状态。
                    - 处理：按当前 TourCode 枚举列出全部引导；没有完成记录的引导视为未完成；新增引导对老用户同样可见。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN。
                    - 响应：返回每种 tourCode 及其 completed 状态。
                    """
    )
    @CheckLogin
    @GetMapping("/listTourStatus")
    public R<List<UserTourStatusResponse>> listTourStatus() {
        return R.ok(userTourService.listTourStatus(SecurityContextHolder.getUserId()));
    }

    @Operation(
            summary = "查询指定引导状态",
            description = """
                    - 用途：判断当前用户是否已完成指定引导。
                    - 请求：tourCode 为必传的 TourCode 枚举值，可选 SYSTEM_FEATURES（系统功能）、COURSE_RESOURCES（课程资源）、NEW_CONVERSATION（新建对话）、NOTE_FEATURES（笔记功能）、PERSONAL_CLOUD_DRIVE（个人云盘）。
                    - 约束：当前用户必须已登录；仅操作当前用户的引导状态。
                    - 处理：查询指定引导的完成事实，不写入记录，不改变任务状态。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN。
                    - 响应：返回是否已完成，无记录时返回 false。
                    """
    )
    @CheckLogin
    @GetMapping("/isTourCompleted")
    public R<Boolean> isTourCompleted(@RequestParam("tourCode") TourCode tourCode) {
        return R.ok(userTourService.isTourCompleted(SecurityContextHolder.getUserId(), tourCode));
    }

    @Operation(
            summary = "汇报引导完成",
            description = """
                    - 用途：记录当前用户完成指定引导，并推进新手教程任务。
                    - 请求：tourCode 为必传的 TourCode 枚举值，可选 SYSTEM_FEATURES（系统功能）、COURSE_RESOURCES（课程资源）、NEW_CONVERSATION（新建对话）、NOTE_FEATURES（笔记功能）、PERSONAL_CLOUD_DRIVE（个人云盘）。
                    - 约束：当前用户必须已登录；仅操作当前用户的引导状态。
                    - 处理：幂等记录完成事实且不可撤销；全部当前引导完成后尝试完成 ONBOARDING_TUTORIAL，遵循其启用配置；不回退已完成任务。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN。
                    - 响应：成功时返回空结果，重复汇报同样成功。
                    """
    )
    @CheckLogin
    @Log(title = "汇报引导完成", businessType = BusinessType.INSERT,
            isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping("/completeTour")
    public R<Void> completeTour(@RequestParam("tourCode") TourCode tourCode) {
        userTourService.completeTour(SecurityContextHolder.getUserId(), tourCode);
        return R.ok();
    }

}
