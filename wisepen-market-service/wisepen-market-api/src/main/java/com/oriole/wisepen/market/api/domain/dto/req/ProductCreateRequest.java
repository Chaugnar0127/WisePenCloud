package com.oriole.wisepen.market.api.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProductCreateRequest implements Serializable {

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "商品价格不能为空")
    @Positive(message = "商品价格必须为正数")
    private Integer price;

    private Integer stock;

    private String category;
}
