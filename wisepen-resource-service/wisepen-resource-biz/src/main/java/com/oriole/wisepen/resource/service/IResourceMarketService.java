package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.resource.domain.dto.req.ResourceMarketQueryRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourcePublishSellRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourcePurchaseRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceReviewSellRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceSubscriptionForkRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceUpdateSellRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceMarketDetailResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceMarketItemResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourcePurchaseResponse;

public interface IResourceMarketService {
    PageR<ResourceMarketItemResponse> listMarketResources(ResourceMarketQueryRequest req, int page, int size);

    ResourceMarketDetailResponse getMarketDetail(String resourceId);

    void publishSellInfo(ResourcePublishSellRequest req);

    void updateSellInfo(ResourceUpdateSellRequest req);

    void offShelfSellInfo(String resourceId, String sellId);

    void reviewSellInfo(ResourceReviewSellRequest req);

    ResourcePurchaseResponse purchase(ResourcePurchaseRequest req);

    String forkLatestBySubscription(ResourceSubscriptionForkRequest req);
}
