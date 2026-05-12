package com.oriole.wisepen.market.api.domain.dto.res;

import com.oriole.wisepen.market.api.domain.base.ProductBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductInfoResponse extends ProductBase {
    private Long productId;
    private Long sellerId;
    private Long groupId;
    private String resourceId;
    private Integer tradeContentType;
    private Integer ownershipTier;
    private Integer grantedActions;
    private Integer price;
    private String category;
    private String tagId;
    private String meta;
    private LocalDateTime createTime;

    // 状态与统计
    private Integer status;
    private Integer viewCount;
    private Integer buyerCount;

    // 聚合展示字段（由服务端填充）
    private String sellerName;
    private Boolean isPurchased;
}