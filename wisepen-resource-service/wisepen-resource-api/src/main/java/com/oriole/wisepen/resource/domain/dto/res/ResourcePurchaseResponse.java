package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.enums.SaleMethod;
import lombok.Data;

@Data
public class ResourcePurchaseResponse {
    private String resourceId;
    private String sellId;
    private SaleMethod saleMethod;
    private String deliveredResourceId;
    private Boolean latestForkAllowed;
}
