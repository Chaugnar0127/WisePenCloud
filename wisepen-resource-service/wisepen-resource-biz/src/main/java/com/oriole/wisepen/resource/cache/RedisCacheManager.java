package com.oriole.wisepen.resource.cache;

import com.oriole.wisepen.resource.event.ResourceGroupDashboardMetricEvent;
import com.oriole.wisepen.user.api.constant.GroupDashboardMetricConstants;
import com.oriole.wisepen.user.api.enums.GroupDashboardSubjectType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisCacheManager {

    /** 阅读量去重窗口时长（分钟）*/
    @Value("${wisepen.resource.read-dedup-ttl-minutes:10}")
    private long readDedupTtlMinutes;

    @Value("${wisepen.group-dashboard-metric-ttl-days:3}")
    private long groupDashboardMetricTtlDays;

    private final StringRedisTemplate stringRedisTemplate;

    private static final String REDIS_READ_DEDUP_PREFIX = "wisepen:resource:read:dedup:";

    /**
     * 阅读去重：在 TTL 窗口内首次访问时置位，返回 true 表示窗口内首次阅读。
     */
    public Boolean isFirstReadInWindow(String resourceId, String userId) {
        String key = REDIS_READ_DEDUP_PREFIX + resourceId + ":" + userId;
        return stringRedisTemplate.opsForValue().setIfAbsent(key, "1", readDedupTtlMinutes, TimeUnit.MINUTES);
    }

    @Async
    @EventListener
    public void incrementResourceGroupDashboardMetric(ResourceGroupDashboardMetricEvent event) {
        LocalDate statDate = LocalDate.now();
        String actorKey = GroupDashboardMetricConstants.actorKey(statDate, event.getGroupId(), GroupDashboardSubjectType.RESOURCE, event.getResourceId(), event.getActorUserId(), event.getMetricType());
        String actorIndexKey = GroupDashboardMetricConstants.actorIndexKey(statDate);

        stringRedisTemplate.opsForValue().increment(actorKey, event.getDelta());
        stringRedisTemplate.opsForSet().add(actorIndexKey, actorKey);

        stringRedisTemplate.expire(actorKey, groupDashboardMetricTtlDays, TimeUnit.DAYS);
        stringRedisTemplate.expire(actorIndexKey, groupDashboardMetricTtlDays, TimeUnit.DAYS);
    }
}
