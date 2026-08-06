package com.oriole.wisepen.resource.event;

import com.oriole.wisepen.user.api.enums.ResourceGroupDashboardMetricType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResourceGroupDashboardMetricEvent {
    private Long groupId;
    private String resourceId;
    private Long actorUserId;
    private ResourceGroupDashboardMetricType metricType;
    private int delta;
}
