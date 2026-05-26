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
    private String sourceResourceId;
    private boolean success;
    private String errorMessage;
    private ResourceType resourceType;
    /** 集市购买订单号，非集市 fork 为空 */
    private Long marketOrderId;
    /** 集市售卖项 ID，非集市 fork 为空 */
    private String marketSellId;
    /** 集市买家用户 ID，非集市 fork 为空 */
    private Long marketBuyerId;

    /**
     * 由 {@link ResourceForkMessage} 构造下游 fork 完成回执。
     *
     * @param resourceType 可覆盖 message 中的类型（如 document 分支显式传入）
     */
    public static ResourceForkCompletedMessage fromFork(ResourceForkMessage message, String newResourceId,
            boolean success, String errorMessage, ResourceType resourceType) {
        ResourceType resolvedType = resourceType != null ? resourceType : message.getResourceType();
        Long marketBuyerId = message.getMarketOrderId() != null && message.getOwnerId() != null
                ? Long.valueOf(message.getOwnerId())
                : null;
        return ResourceForkCompletedMessage.builder()
                .newResourceId(newResourceId)
                .sourceResourceId(message.getSourceResourceId())
                .marketOrderId(message.getMarketOrderId())
                .marketSellId(message.getMarketSellId())
                .marketBuyerId(marketBuyerId)
                .success(success)
                .errorMessage(errorMessage)
                .resourceType(resolvedType)
                .build();
    }
}
