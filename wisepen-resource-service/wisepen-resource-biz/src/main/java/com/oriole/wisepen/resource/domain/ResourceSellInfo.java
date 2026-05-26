package com.oriole.wisepen.resource.domain;

import com.oriole.wisepen.resource.domain.base.SellInfoBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceSellInfo extends SellInfoBase {
    private Boolean offShelf;
    private SellReviewInfo admin;
}
