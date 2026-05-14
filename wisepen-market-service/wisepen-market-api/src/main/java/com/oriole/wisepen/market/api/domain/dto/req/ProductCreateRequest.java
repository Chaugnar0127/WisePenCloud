package com.oriole.wisepen.market.api.domain.dto.req;

import com.oriole.wisepen.market.api.domain.base.ProductBase;
import com.oriole.wisepen.market.api.enums.TradeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProductCreateRequest extends ProductBase {
    @NotNull(message = "商品ID不能为空", groups = {UpdataProduct.class})
    private Long productId;

    @NotNull(message = "商品价格不能为空", groups = {CreateProduct.class})
    @Positive(message = "商品价格必须为正数")
    private Integer price;

    private String category;

    private Long groupId;

    @NotNull(message = "资源ID不能为空", groups = {CreateProduct.class})
    private Long resourceId;

    @NotNull(message = "交易类型不能为空", groups = {CreateProduct.class})
    private TradeType tradeContentType;

    private Integer ownershipTier;

    private Integer grantedActions;

    @NotNull(message = "标签ID不能为空", groups = {CreateProduct.class})
    private Long tagId;
}