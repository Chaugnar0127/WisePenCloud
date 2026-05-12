package com.oriole.wisepen.market.api.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProductCreateRequest implements Serializable {

    private Long productId;

    @NotBlank(message = "商品名称不能为空")
    private String productName;

    private String productDesc;

    @NotNull(message = "商品价格不能为空")
    @Positive(message = "商品价格必须为正数")
    private Integer price;

    private String category;

    private Long groupId;

    @NotBlank(message = "资源ID不能为空")
    private Long resourceId;

    @NotNull(message = "交易类型不能为空")
    private Integer tradeContentType;

    private Integer ownershipTier;

    private Integer grantedActions;

    @NotBlank(message = "标签ID不能为空")
    private Long tagId;
}