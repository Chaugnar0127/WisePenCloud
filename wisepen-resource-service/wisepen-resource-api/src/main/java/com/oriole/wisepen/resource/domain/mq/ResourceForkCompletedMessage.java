package com.oriole.wisepen.resource.domain.mq;

import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 下游 fork 完成回执：note/document 复制内容后发布，resource 服务更新生命周期状态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceForkCompletedMessage implements Serializable {
    private String newResourceId;
    private boolean success;
    private String errorMessage;
    private ResourceType resourceType;
}
