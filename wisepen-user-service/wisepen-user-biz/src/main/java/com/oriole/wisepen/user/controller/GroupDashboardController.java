package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.user.api.domain.dto.res.GroupDashboardActorMetricResponse;
import com.oriole.wisepen.user.api.domain.dto.res.GroupDashboardResourceMetricResponse;
import com.oriole.wisepen.user.api.domain.dto.res.GroupResourceDailyMetricResponse;
import com.oriole.wisepen.user.service.IGroupDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "小组看板", description = "小组资源工作情况统计与成员行为聚合")
@RestController
@RequestMapping("/group/dashboard")
@RequiredArgsConstructor
@Validated
@CheckLogin
public class GroupDashboardController {

    private final IGroupDashboardService groupDashboardService;

    @Operation(
            summary = "查询小组资源每日指标",
            description = """
                    - 用途：小组管理员查看指定日期范围内每天的资源新增、阅读、点赞、评论和 AI 调用统计。
                    - 请求：groupId 指定目标小组；startDate 和 endDate 使用 yyyy-MM-dd。
                    - 约束：当前用户必须是目标小组 OWNER 或 ADMIN。
                    - 处理：读取已由 Redis counter 聚合落库的小组资源日维总量；不叠加尚未聚合的 Redis 增量。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN；当前用户不是 OWNER 或 ADMIN -> PermissionError.PERMISSION_DENIED。
                    - 响应：返回日期范围内每天一条指标；不返回区间总计。
                    """
    )
    @GetMapping("/listResourceDailyMetrics")
    public R<List<GroupResourceDailyMetricResponse>> listResourceDailyMetrics(
            @RequestParam Long groupId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate endDate) {
        SecurityContextHolder.assertGroupRole(groupId, GroupRoleType.OWNER, GroupRoleType.ADMIN);
        return R.ok(groupDashboardService.listResourceDailyMetrics(groupId, startDate, endDate));
    }

    @Operation(
            summary = "分页查询小组资源指标",
            description = """
                    - 用途：小组管理员按资源查看指定日期的小组资源贡献指标。
                    - 请求：groupId 指定目标小组；statDate 使用 yyyy-MM-dd；page、size 控制分页。
                    - 约束：当前用户必须是目标小组 OWNER 或 ADMIN；page 不能小于 1；size 不能超过 100。
                    - 处理：读取已落库的资源日维贡献指标并批量补充资源名称和类型；默认按资源阅读数倒序分页；不返回原始行为流水。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN；当前用户不是 OWNER 或 ADMIN -> PermissionError.PERMISSION_DENIED。
                    - 响应：返回分页资源指标列表。
                    """
    )
    @GetMapping("/listResourceMetrics")
    public R<PageR<GroupDashboardResourceMetricResponse>> listResourceMetrics(
            @RequestParam Long groupId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate statDate,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
        SecurityContextHolder.assertGroupRole(groupId, GroupRoleType.OWNER, GroupRoleType.ADMIN);
        return R.ok(groupDashboardService.listResourceMetrics(groupId, statDate, page, size));
    }

    @Operation(
            summary = "分页查询资源成员指标",
            description = """
                    - 用途：小组管理员查看指定日期单个资源下各成员产生的阅读、点赞、评论和 AI 调用统计。
                    - 请求：groupId 指定目标小组；resourceId 指定资源；statDate 使用 yyyy-MM-dd；page 和 size 控制分页。
                    - 约束：当前用户必须是目标小组 OWNER 或 ADMIN；page 不能小于 1；size 不能超过 100。
                    - 处理：读取已落库的资源成员日维贡献指标并批量补充用户展示信息；不返回原始行为流水。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN；当前用户不是 OWNER 或 ADMIN -> PermissionError.PERMISSION_DENIED。
                    - 响应：返回分页成员指标列表。
                    """
    )
    @GetMapping("/listResourceActorMetrics")
    public R<PageR<GroupDashboardActorMetricResponse>> listResourceActorMetrics(
            @RequestParam Long groupId,
            @RequestParam @NotBlank String resourceId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate statDate,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
        SecurityContextHolder.assertGroupRole(groupId, GroupRoleType.OWNER, GroupRoleType.ADMIN);
        return R.ok(groupDashboardService.listResourceActorMetrics(groupId, resourceId, statDate, page, size));
    }
}
