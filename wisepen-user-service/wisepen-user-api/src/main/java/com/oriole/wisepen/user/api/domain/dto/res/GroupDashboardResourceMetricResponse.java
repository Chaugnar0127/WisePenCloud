package com.oriole.wisepen.user.api.domain.dto.res;

import com.oriole.wisepen.user.api.domain.base.GroupResourceMetricBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupDashboardResourceMetricResponse extends GroupResourceMetricBase {
    private String resourceId;
    private String resourceName;
    private String resourceType;
}
