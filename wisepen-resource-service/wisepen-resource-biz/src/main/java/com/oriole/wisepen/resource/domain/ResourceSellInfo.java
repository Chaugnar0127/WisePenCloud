package com.oriole.wisepen.resource.domain;

import com.oriole.wisepen.resource.domain.base.SellInfoBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceSellInfo extends SellInfoBase {
    private Boolean offShelf;
    private SellReviewInfo admin;
    private Set<String> purchasedBuyerIds;

    public Set<String> getPurchasedBuyerIds() {
        if (purchasedBuyerIds == null) {
            purchasedBuyerIds = new HashSet<>();
        }
        return purchasedBuyerIds;
    }
}
