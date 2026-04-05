package com.oriole.wisepen.resource.domain.mq;

import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 资源硬删除广播事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDeletedMessage {
    /**
     * 按资源类型分组的待删除资源ID列表
     */
    private Map<ResourceType, List<String>> typedResourceIds;
}