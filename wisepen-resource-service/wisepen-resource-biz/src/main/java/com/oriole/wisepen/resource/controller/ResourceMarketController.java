package com.oriole.wisepen.resource.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.resource.domain.dto.req.ResourcePurchaseRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourcePublishSellRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceReviewSellRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceMarketDetailResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourcePurchaseResponse;
import com.oriole.wisepen.resource.service.IResourceMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "资源市场", description = "商品上架与审核；市场目录请用 GET /resource/item/listResources")
@RestController
@RequestMapping("/resource/market")
@RequiredArgsConstructor
@CheckLogin
public class ResourceMarketController {

    private final IResourceMarketService resourceMarketService;

    @Operation(summary = "商品详情", description = "返回该资源在指定市场小组下已审核可购的售卖项")
    @GetMapping("/getProductDetail")
    public R<ResourceMarketDetailResponse> getMarketDetail(
            @RequestParam("resourceId") String resourceId,
            @RequestParam("groupId") String groupId) {
        SecurityContextHolder.assertInGroup(Long.valueOf(groupId));
        return R.ok(resourceMarketService.getMarketDetail(resourceId, groupId));
    }

    @Operation(summary = "上架资源", description = "资源所有者将资源提交到指定小组市场（待审核）")
    @Log(title = "上架资源", businessType = BusinessType.INSERT)
    @PostMapping("/addProduct")
    public R<String> publishSellInfo(@Validated @RequestBody ResourcePublishSellRequest req) {
        Long userId = SecurityContextHolder.getUserId();
        SecurityContextHolder.assertInGroup(Long.valueOf(req.getGroupId()));
        return R.ok(resourceMarketService.publishSellInfo(req, userId));
    }

    @Operation(summary = "审核上架信息", description = "系统管理员、小组管理员或所有者审核资源上架")
    @Log(title = "审核上架信息", businessType = BusinessType.UPDATE)
    @PostMapping("/reviewProduct")
    public R<Void> reviewSellInfo(@Validated @RequestBody ResourceReviewSellRequest req) {
        resourceMarketService.reviewSellInfo(req,
                SecurityContextHolder.getUserId(),
                SecurityContextHolder.getIdentityType(),
                SecurityContextHolder.getGroupRoleMap());
        return R.ok();
    }

    @Operation(summary = "购买资源", description = "orderId 由 sellId+buyerId 生成（钱包 relatedId 唯一）；扣款后写入 purchasedBuyerIds 并 fork，失败则冲正")
    @Log(title = "购买资源", businessType = BusinessType.INSERT)
    @PostMapping("/purchaseProduct")
    public R<ResourcePurchaseResponse> purchaseProduct(@Validated @RequestBody ResourcePurchaseRequest req) {
        SecurityContextHolder.assertInGroup(Long.valueOf(req.getGroupId()));
        return R.ok(resourceMarketService.purchaseProduct(req, SecurityContextHolder.getUserId()));
    }
}
