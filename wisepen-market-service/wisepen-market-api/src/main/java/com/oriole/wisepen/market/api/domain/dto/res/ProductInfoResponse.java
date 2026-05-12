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
    private Integer stock;
    private String category;
    private String tagId;
    private String meta;
    private LocalDateTime createTime;
}