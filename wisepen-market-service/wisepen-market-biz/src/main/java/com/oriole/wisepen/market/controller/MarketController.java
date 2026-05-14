package com.oriole.wisepen.market.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.market.api.domain.dto.req.CreateProduct;
import com.oriole.wisepen.market.api.domain.dto.req.ProductCreateRequest;
import com.oriole.wisepen.market.api.domain.dto.req.ProductSearchRequest;
import com.oriole.wisepen.market.api.domain.dto.req.UpdataProduct;
import com.oriole.wisepen.market.api.domain.dto.res.ProductInfoResponse;
import com.oriole.wisepen.market.service.IMarketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.websocket.server.UpgradeUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
@CheckLogin
public class MarketController {

    private final IMarketService marketService;

    @GetMapping("/shop/getProductList")
    public R<PageResult<ProductInfoResponse>> getProductList(@RequestBody ProductSearchRequest req, @RequestParam Integer page, @RequestParam Integer size) {
        return R.ok(marketService.getProductList(req, page, size));
    }

    @GetMapping("/shop/getProductDetail")
    public R<ProductInfoResponse> getProductDetail(@RequestParam Long productId) {
        return R.ok(marketService.getProductDetail(productId));
    }

    @PostMapping("/shop/addProduct")
    public R<Void> addProduct(@RequestBody @Validated(CreateProduct.class) ProductCreateRequest req) {
        marketService.addProduct(req);
        return R.ok();
    }

    @PostMapping("/shop/updateProduct")
    public R<Void> updateProduct(@RequestBody @Validated(UpdataProduct.class) ProductCreateRequest req) {
        marketService.updateProduct(req);
        return R.ok();
    }

    @PostMapping("/shop/purchase")
    public R<Void> purchase(@RequestParam Long productId) {
        marketService.purchase(productId);
        return R.ok();
    }

    @PostMapping("/shop/deleteProduct")
    public R<Void> deleteProduct(@RequestParam Long productId) {
        marketService.deleteProduct(productId);
        return R.ok();
    }

    @GetMapping("/shop/getMyList")
    public R<PageResult<ProductInfoResponse>> getMyList(@RequestParam Integer page, @RequestParam Integer size) {
        return R.ok(marketService.getMyList(page, size));
    }
}
