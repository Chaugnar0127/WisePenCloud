package com.oriole.wisepen.market.service;

import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.market.api.domain.dto.req.ProductCreateRequest;
import com.oriole.wisepen.market.api.domain.dto.req.ProductSearchRequest;
import com.oriole.wisepen.market.api.domain.dto.res.ProductInfoResponse;

public interface IMarketService {
    PageResult<ProductInfoResponse> getProductList(ProductSearchRequest dto, Integer page, Integer size);
    ProductInfoResponse getProductDetail(Long productId);
    void addProduct(ProductCreateRequest dto);
    void updateProduct(ProductCreateRequest dto);
    void purchase(Long productId, Long buyerId);
    PageResult<ProductInfoResponse> getMyList(Long userLd, Integer page, Integer size);
    void deleteProduct(Long productId);
}
