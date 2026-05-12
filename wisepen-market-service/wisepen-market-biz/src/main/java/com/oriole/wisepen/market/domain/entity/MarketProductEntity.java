package com.oriole.wisepen.market.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oriole.wisepen.market.api.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_mkt_product")
public class MarketProductEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long productId;

    private String productName;

    private Long sellerId;

    private Long groupId;

    private Long resourceId;

    private Integer tradeContentType;

    private Integer ownershipTier;

    private Integer grantedActions;

    private Integer price;

    private Integer stock;

    private String category;

    private String productDesc;

    private Long tagId;

    private String meta;

    private ProductStatus status;

    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    private Integer buyerCount = 0;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}