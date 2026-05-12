package com.oriole.wisepen.market.api.domain.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ProductBase {
    private String productName;
    private String productDesc;
}
