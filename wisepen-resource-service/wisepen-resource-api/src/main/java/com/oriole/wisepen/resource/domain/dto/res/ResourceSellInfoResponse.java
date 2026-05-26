package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.domain.base.SellInfoBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceSellInfoResponse extends SellInfoBase {
    private Boolean offShelf;
    private Boolean approved;
    private String reviewComment;
}
