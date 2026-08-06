package com.oriole.wisepen.user.api.domain.base;

import com.oriole.wisepen.user.api.enums.ResourceGroupDashboardMetricType;
import lombok.Data;

@Data
public class GroupResourceMetricBase {
    private Integer resourceAddedCount = 0;
    private Integer resourceReadCount = 0;
    private Integer resourceLikeCount = 0;
    private Integer resourceCommentCount = 0;
    private Integer resourceAiCallCount = 0;

    public void setMetricValue(ResourceGroupDashboardMetricType metricType, Integer value) {
        switch (metricType) {
            case RESOURCE_ADDED -> this.resourceAddedCount = value;
            case RESOURCE_READ -> this.resourceReadCount = value;
            case RESOURCE_LIKE -> this.resourceLikeCount = value;
            case RESOURCE_COMMENT -> this.resourceCommentCount = value;
            case RESOURCE_AI_CALL -> this.resourceAiCallCount = value;
        }
    }

    public void addMetricValue(ResourceGroupDashboardMetricType metricType, Integer value) {
        switch (metricType) {
            case RESOURCE_ADDED -> this.resourceAddedCount += value;
            case RESOURCE_READ -> this.resourceReadCount += value;
            case RESOURCE_LIKE -> this.resourceLikeCount += value;
            case RESOURCE_COMMENT -> this.resourceCommentCount += value;
            case RESOURCE_AI_CALL -> this.resourceAiCallCount += value;
        }
    }

    public void addMetricValue(GroupResourceMetricBase metricValue) {
        this.resourceAddedCount += metricValue.getResourceAddedCount();
        this.resourceReadCount += metricValue.getResourceReadCount();
        this.resourceLikeCount += metricValue.getResourceLikeCount();
        this.resourceCommentCount += metricValue.getResourceCommentCount();
        this.resourceAiCallCount += metricValue.getResourceAiCallCount();
    }
}
