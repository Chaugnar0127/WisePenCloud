package com.oriole.wisepen.market.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.market.api.domain.dto.req.ProductCreateRequest;
import com.oriole.wisepen.market.api.domain.dto.req.ProductSearchRequest;
import com.oriole.wisepen.market.api.domain.dto.res.ProductInfoResponse;
import com.oriole.wisepen.market.domain.entity.MarketOrderEntity;
import com.oriole.wisepen.market.domain.entity.MarketProductEntity;
import com.oriole.wisepen.market.exception.MarketErrorCode;
import com.oriole.wisepen.market.mapper.MarketOrderMapper;
import com.oriole.wisepen.market.mapper.MarketProductMapper;
import com.oriole.wisepen.market.service.IInfoPointService;
import com.oriole.wisepen.market.service.IMarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IMarketServiceImpl implements IMarketService {

    private final MarketProductMapper marketProductMapper;
    private final MarketOrderMapper marketOrderMapper;
    private final IInfoPointService infoPointService;

    @Override
    public PageResult<ProductInfoResponse> getProductList(ProductSearchRequest dto, Integer page, Integer size) {
        // TODO: 关键词搜素/全文搜索
        Page<MarketProductEntity> recordPage = new Page<>(page, size);

        LambdaQueryWrapper<MarketProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MarketProductEntity::getGroupId, dto.getGroupId());
        // TODO: tag/类型搜索

        if (dto.getSortBy() == null) {
            wrapper.orderByDesc(MarketProductEntity::getCreateTime);
        } else {
            switch (dto.getSortBy()) {
                case TIME_ASC:
                    wrapper.orderByAsc(MarketProductEntity::getCreateTime);
                    break;
                case TIME_DESC:
                    wrapper.orderByDesc(MarketProductEntity::getCreateTime);
                    break;
                case PRICE_ASC:
                    wrapper.orderByAsc(MarketProductEntity::getPrice);
                    break;
                case PRICE_DESC:
                    wrapper.orderByDesc(MarketProductEntity::getPrice);
                    break;
                default:
                    wrapper.orderByDesc(MarketProductEntity::getCreateTime);
            }
        }

        // 分页查询
        Page<MarketProductEntity> resultPage = marketProductMapper.selectPage(recordPage, wrapper);
        PageResult<ProductInfoResponse> pageResult = new PageResult<>(resultPage.getTotal(), page, size);

        List<MarketProductEntity> records = resultPage.getRecords();
        if (records.isEmpty()) {
            return pageResult;
        }

        List<ProductInfoResponse> responses = records.stream()
                .map(record -> BeanUtil.copyProperties(record, ProductInfoResponse.class))
                .collect(Collectors.toList());

        pageResult.addAll(responses);
        return pageResult;
    }

    @Override
    public ProductInfoResponse getProductDetail(Long productId) {
        MarketProductEntity product = marketProductMapper.selectById(productId);
        ProductInfoResponse productInfoResponse = new ProductInfoResponse();
        BeanUtil.copyProperties(product, productInfoResponse);
        return productInfoResponse;
    }

    @Override
    public void addProduct(ProductCreateRequest dto) {
        MarketProductEntity product = BeanUtil.copyProperties(dto, MarketProductEntity.class);
        marketProductMapper.insert(product);
    }

    @Override
    public void updateProduct(ProductCreateRequest dto) {
        Long productId = dto.getProductId();
        MarketProductEntity product = marketProductMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException(MarketErrorCode.PRODUCT_NOT_FOUND);
        }

        // 默认只更新非 null 字段，防一下不能更新的字段
        BeanUtil.copyProperties(dto, product, "productId", "sellerId", "createTime");
        marketProductMapper.updateById(product);
    }

    @Override
    public void purchase(Long productId, Long buyerId) {
        MarketProductEntity product = marketProductMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException(MarketErrorCode.PRODUCT_NOT_FOUND);
        }

        if (product.getSellerId() != null && product.getSellerId().equals(buyerId)) {
            throw new ServiceException(MarketErrorCode.SELF_TRANSACTION_NOT_ALLOWED);
        }

        if (product.getStock() != null && product.getStock() <= 0) {
            throw new ServiceException(MarketErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }

        // 新建订单
        MarketOrderEntity order = MarketOrderEntity.builder()
                .productId(productId)
                .sellerId(product.getSellerId())
                .buyerId(buyerId)
                .build();
        marketOrderMapper.insert(order);

        // 处理订单
        infoPointService.handleTransaction(buyerId,order.getSellerId(),product.getPrice(),order.getOrderId());
    }

    @Override
    public PageResult<ProductInfoResponse> getMyList(Long userId, Integer page, Integer size) {
        Page<MarketProductEntity> recordPage = new Page<>(page, size);

        LambdaQueryWrapper<MarketProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MarketProductEntity::getSellerId, userId);
        wrapper.orderByDesc(MarketProductEntity::getCreateTime);


        // 分页查询
        Page<MarketProductEntity> resultPage = marketProductMapper.selectPage(recordPage, wrapper);
        PageResult<ProductInfoResponse> pageResult = new PageResult<>(resultPage.getTotal(), page, size);

        List<MarketProductEntity> records = resultPage.getRecords();
        if (records.isEmpty()) {
            return pageResult;
        }

        List<ProductInfoResponse> responses = records.stream()
                .map(record -> BeanUtil.copyProperties(record, ProductInfoResponse.class))
                .collect(Collectors.toList());

        pageResult.addAll(responses);
        return pageResult;
    }

    @Override
    public void deleteProduct(Long productId) {
        MarketProductEntity product = marketProductMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException(MarketErrorCode.PRODUCT_NOT_FOUND);
        }
        marketProductMapper.deleteById(productId);
    }

}