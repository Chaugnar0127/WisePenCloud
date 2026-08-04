package com.oriole.wisepen.user.api.constant;

import com.oriole.wisepen.user.api.enums.GroupDashboardSubjectType;
import com.oriole.wisepen.user.api.enums.ResourceGroupDashboardMetricType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public interface GroupDashboardMetricConstants {
    String ACTOR_KEY_PREFIX = "wisepen:group:metric:user";
    String ACTOR_INDEX_KEY_PREFIX = "wisepen:group:metric:user-keys";
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    static String formatDate(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    // 用户维度指标索引集合
    static String actorIndexKey(LocalDate date) {
        return ACTOR_INDEX_KEY_PREFIX + ":" + formatDate(date);
    }

    // 用户维度 key，表示某个小组的某个用户对某天某个对象某个指标的贡献
    // wisepen:group:metric:user:{date}:{groupId}:{subjectType}:{subjectId}:{actorUserId}:{metricType}
    static String actorKey(LocalDate date, Long groupId, GroupDashboardSubjectType subjectType,
                           String subjectId, Long actorUserId, ResourceGroupDashboardMetricType metricType) {
        return ACTOR_KEY_PREFIX + ":" + formatDate(date) + ":" + groupId + ":" +
                subjectType.getValue() + ":" + subjectId + ":" + actorUserId + ":" + metricType.getValue();
    }
}
