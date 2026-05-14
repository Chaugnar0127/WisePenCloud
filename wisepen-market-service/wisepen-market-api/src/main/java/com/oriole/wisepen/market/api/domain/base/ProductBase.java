package com.oriole.wisepen.market.api.domain.base;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
public class ProductBase implements Serializable {
    @NotBlank(message = "商品名称不能为空")
    private String productName;
    private String productDesc;
}
