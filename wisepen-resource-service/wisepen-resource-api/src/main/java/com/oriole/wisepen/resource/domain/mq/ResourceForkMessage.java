package com.oriole.wisepen.resource.domain.mq;

import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 资源 Fork 广播：通知 note/document 等下游复制内容到新 resourceId。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceForkMessage implements Serializable {
    private String sourceResourceId;
    private String newResourceId;
    private ResourceType resourceType;
    /** 笔记快照版本，空表示最新 */
    private Long version;
    private String forkedBy;
    private String ownerId;
    /** 集市购买订单号，非集市 fork 为空 */
    private Long marketOrderId;
    /** 集市售卖项 ID，非集市 fork 为空 */
    private String marketSellId;
}
