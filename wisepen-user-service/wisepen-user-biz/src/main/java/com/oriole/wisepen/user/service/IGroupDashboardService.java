package com.oriole.wisepen.user.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.user.api.domain.dto.res.GroupDashboardActorMetricResponse;
import com.oriole.wisepen.user.api.domain.dto.res.GroupDashboardResourceMetricResponse;
import com.oriole.wisepen.user.api.domain.dto.res.GroupResourceDailyMetricResponse;

import java.time.LocalDate;
import java.util.List;

public interface IGroupDashboardService {
    void aggregateRecentResourceMetrics();

    List<GroupResourceDailyMetricResponse> listResourceDailyMetrics(Long groupId, LocalDate startDate, LocalDate endDate);

    PageR<GroupDashboardResourceMetricResponse> listResourceMetrics(Long groupId, LocalDate statDate,
                                                                     int page, int size);

    PageR<GroupDashboardActorMetricResponse> listResourceActorMetrics(Long groupId, String resourceId,
                                                                       LocalDate statDate,
                                                                       int page, int size);
}
