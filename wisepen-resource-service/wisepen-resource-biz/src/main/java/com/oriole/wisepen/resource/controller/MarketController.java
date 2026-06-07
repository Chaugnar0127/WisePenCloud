package com.oriole.wisepen.resource.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.resource.domain.dto.req.MarketAuditListingRequest;
import com.oriole.wisepen.resource.domain.dto.req.MarketForkRequest;
import com.oriole.wisepen.resource.domain.dto.req.MarketListResourceRequest;
import com.oriole.wisepen.resource.domain.dto.req.MarketOffShelfRequest;
import com.oriole.wisepen.resource.domain.dto.req.MarketPurchaseRequest;
import com.oriole.wisepen.resource.domain.dto.res.MarketListingResponse;
import com.oriole.wisepen.resource.domain.dto.res.MarketPurchaseResponse;
import com.oriole.wisepen.resource.service.IMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "集市", description = "资源上架、购买和复制")
@RestController
@RequestMapping("/resource/market")
@RequiredArgsConstructor
@CheckLogin
@Validated
public class MarketController {

    private final IMarketService marketService;

    @Operation(summary = "上架资源")
    @Log(title = "上架资源", businessType = BusinessType.INSERT)
    @PostMapping("/addListing")
    public R<MarketListingResponse> addListing(@Valid @RequestBody MarketListResourceRequest request) {
        return R.ok(marketService.addListing(request, SecurityContextHolder.getUserId(), SecurityContextHolder.getGroupRoleMap()));
    }

    @Operation(summary = "下架资源")
    @Log(title = "下架资源", businessType = BusinessType.UPDATE)
    @PostMapping("/offShelfListing")
    public R<Void> offShelfListing(@Valid @RequestBody MarketOffShelfRequest request) {
        marketService.offShelfListing(request, SecurityContextHolder.getUserId(), SecurityContextHolder.getGroupRoleMap());
        return R.ok();
    }

    @Operation(summary = "审核上架")
    @Log(title = "审核上架", businessType = BusinessType.UPDATE)
    @PostMapping("/auditListing")
    public R<MarketListingResponse> auditListing(@Valid @RequestBody MarketAuditListingRequest request) {
        return R.ok(marketService.auditListing(
                request,
                SecurityContextHolder.getUserId(),
                SecurityContextHolder.getGroupRoleMap()
        ));
    }

    @Operation(summary = "购买资源")
    @Log(title = "购买资源", businessType = BusinessType.INSERT)
    @PostMapping("/purchaseListing")
    public R<MarketPurchaseResponse> purchaseListing(@Valid @RequestBody MarketPurchaseRequest request) {
        return R.ok(marketService.purchaseListing(
                request,
                SecurityContextHolder.getUserId(),
                SecurityContextHolder.getGroupRoleMap()
        ));
    }

    @Operation(summary = "复制已购买资源")
    @Log(title = "复制已购买资源", businessType = BusinessType.INSERT)
    @PostMapping("/forkPurchase")
    public R<MarketPurchaseResponse> forkPurchase(@Valid @RequestBody MarketForkRequest request) {
        return R.ok(marketService.forkPurchase(request, SecurityContextHolder.getUserId()));
    }

    @Operation(summary = "我的购买")
    @GetMapping("/listMyPurchases")
    public R<PageR<MarketPurchaseResponse>> listMyPurchases(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) int size) {
        return R.ok(marketService.listMyPurchases(SecurityContextHolder.getUserId().toString(), page, size));
    }
}
