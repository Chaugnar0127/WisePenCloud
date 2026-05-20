package com.oriole.wisepen.resource.controller;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.resource.domain.dto.req.ResourceMarketQueryRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourcePublishSellRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourcePurchaseRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceReviewSellRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceSubscriptionForkRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceUpdateSellRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceMarketDetailResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceMarketItemResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourcePurchaseResponse;
import com.oriole.wisepen.resource.enums.ResourceSortBy;
import com.oriole.wisepen.resource.enums.SaleMethod;
import com.oriole.wisepen.resource.service.IResourceMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "资源市场", description = "资源上架、市场查询、购买与订阅 fork")
@RestController
@RequestMapping("/resource/market")
@RequiredArgsConstructor
@CheckLogin
public class ResourceMarketController {

    private final IResourceMarketService resourceMarketService;

    @Operation(summary = "市场资源列表", description = "按小组、标签、资源类型和售卖方式查询在售资源")
    @GetMapping("/list")
    public R<PageR<ResourceMarketItemResponse>> listMarketResources(
            @RequestParam("groupId") String groupId,
            @RequestParam(value = "tagIds", required = false) List<String> tagIds,
            @RequestParam(value = "resourceType", required = false) String resourceType,
            @RequestParam(value = "saleMethod", required = false) SaleMethod saleMethod,
            @Parameter(description = "排序字段枚举")
            @RequestParam(value = "sortBy", defaultValue = "UPDATE_TIME") ResourceSortBy sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        ResourceMarketQueryRequest req = new ResourceMarketQueryRequest();
        req.setGroupId(groupId);
        req.setTagIds(tagIds);
        req.setResourceType(resourceType);
        req.setSaleMethod(saleMethod);
        req.setSortBy(sortBy);
        return R.ok(resourceMarketService.listMarketResources(req, page, size));
    }

    @Operation(summary = "市场资源详情", description = "查询资源在售详情")
    @GetMapping("/detail")
    public R<ResourceMarketDetailResponse> getMarketDetail(@RequestParam("resourceId") String resourceId) {
        return R.ok(resourceMarketService.getMarketDetail(resourceId));
    }

    @Operation(summary = "上架资源", description = "资源所有者将资源上架到指定小组市场")
    @Log(title = "上架资源", businessType = BusinessType.INSERT)
    @PostMapping("/publish")
    public R<Void> publishSellInfo(@Validated @RequestBody ResourcePublishSellRequest req) {
        resourceMarketService.publishSellInfo(req);
        return R.ok();
    }

    @Operation(summary = "修改上架信息", description = "资源所有者修改价格、预览方式或市场标签")
    @Log(title = "修改上架信息", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public R<Void> updateSellInfo(@Validated @RequestBody ResourceUpdateSellRequest req) {
        resourceMarketService.updateSellInfo(req);
        return R.ok();
    }

    @Operation(summary = "下架资源", description = "资源所有者下架指定售卖项")
    @Log(title = "下架资源", businessType = BusinessType.UPDATE)
    @PostMapping("/offShelf")
    public R<Void> offShelfSellInfo(@RequestParam("resourceId") String resourceId,
                                    @RequestParam("sellId") String sellId) {
        resourceMarketService.offShelfSellInfo(resourceId, sellId);
        return R.ok();
    }

    @Operation(summary = "审核上架信息", description = "系统管理员、小组管理员或所有者审核资源上架")
    @Log(title = "审核上架信息", businessType = BusinessType.UPDATE)
    @PostMapping("/review")
    public R<Void> reviewSellInfo(@Validated @RequestBody ResourceReviewSellRequest req) {
        resourceMarketService.reviewSellInfo(req);
        return R.ok();
    }

    @Operation(summary = "购买资源", description = "购买使用权、所有权或订阅")
    @Log(title = "购买资源", businessType = BusinessType.INSERT)
    @PostMapping("/purchase")
    public R<ResourcePurchaseResponse> purchase(@Validated @RequestBody ResourcePurchaseRequest req) {
        return R.ok(resourceMarketService.purchase(req));
    }

    @Operation(summary = "订阅 fork 最新版", description = "订阅者基于订阅权限 fork 当前最新版本")
    @Log(title = "订阅 fork 最新版", businessType = BusinessType.INSERT)
    @PostMapping("/subscription/forkLatest")
    public R<String> forkLatestBySubscription(@Validated @RequestBody ResourceSubscriptionForkRequest req) {
        return R.ok(resourceMarketService.forkLatestBySubscription(req));
    }
}
