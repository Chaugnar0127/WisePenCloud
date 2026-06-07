package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.resource.domain.dto.req.MarketAuditListingRequest;
import com.oriole.wisepen.resource.domain.dto.req.MarketForkRequest;
import com.oriole.wisepen.resource.domain.dto.req.MarketListResourceRequest;
import com.oriole.wisepen.resource.domain.dto.req.MarketOffShelfRequest;
import com.oriole.wisepen.resource.domain.dto.req.MarketPurchaseRequest;
import com.oriole.wisepen.resource.domain.dto.res.MarketListingResponse;
import com.oriole.wisepen.resource.domain.dto.res.MarketPurchaseResponse;

import java.util.Map;

public interface IMarketService {

    MarketListingResponse addListing(MarketListResourceRequest request, Long sellerId, Map<Long, GroupRoleType> groupRoles);

    void offShelfListing(MarketOffShelfRequest request, Long operatorId, Map<Long, GroupRoleType> groupRoles);

    MarketListingResponse auditListing(MarketAuditListingRequest request, Long operatorId, Map<Long, GroupRoleType> groupRoles);

    MarketPurchaseResponse purchaseListing(MarketPurchaseRequest request, Long buyerId, Map<Long, GroupRoleType> groupRoles);

    MarketPurchaseResponse forkPurchase(MarketForkRequest request, Long buyerId);

    PageR<MarketPurchaseResponse> listMyPurchases(String buyerId, int page, int size);
}
