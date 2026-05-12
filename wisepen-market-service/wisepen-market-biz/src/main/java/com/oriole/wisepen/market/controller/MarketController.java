package com.oriole.wisepen.market.controller;

import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.market.api.domain.dto.req.ProductCreateRequest;
import com.oriole.wisepen.market.api.domain.dto.req.ProductSearchRequest;
import com.oriole.wisepen.market.api.domain.dto.res.ProductInfoResponse;
import com.oriole.wisepen.market.service.IMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {

    private final IMarketService marketService;

    @GetMapping("/shop/getProductList")
    public R<PageResult<ProductInfoResponse>> getProductList(@RequestParam ProductSearchRequest req, @RequestParam Integer page, @RequestParam Integer size) {
        return R.ok(marketService.getProductList(req, page, size));
    }

    @GetMapping("/shop/getProductDetail")
    public R<ProductInfoResponse> getProductDetail(@RequestParam Long productId) {
        return R.ok(marketService.getProductDetail(productId));
    }

    @PostMapping("/shop/addProduct")
    public R<Void> addProduct(@RequestParam ProductCreateRequest req) {
        marketService.addProduct(req);
        return R.ok();
    }

    @PostMapping("/shop/updateProduct")
    public R<Void> updateProduct(@RequestParam ProductCreateRequest req) {
        marketService.updateProduct(req);
        return R.ok();
    }

    @PostMapping("/shop/purchase")
    public R<Void> purchase(@RequestParam Long productId, @RequestParam Long buyerId) {
        marketService.purchase(productId, buyerId);
        return R.ok();
    }

    @GetMapping("/shop/getMyList")
    public R<PageResult<ProductInfoResponse>> getMyList(@RequestParam Long userId, @RequestParam Integer page, @RequestParam Integer size) {
        return R.ok(marketService.getMyList(userId, page, size));
    }
}
