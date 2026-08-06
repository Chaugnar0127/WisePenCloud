package com.oriole.wisepen.user.api.domain.dto.res;

import com.oriole.wisepen.user.api.domain.base.GroupResourceMetricBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupResourceDailyMetricResponse extends GroupResourceMetricBase {
    private LocalDate statDate;
}
