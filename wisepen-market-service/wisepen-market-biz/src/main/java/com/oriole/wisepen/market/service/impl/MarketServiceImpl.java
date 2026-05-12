package com.oriole.wisepen.market.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.market.api.domain.dto.req.ProductCreateRequest;
import com.oriole.wisepen.market.api.domain.dto.req.ProductSearchRequest;
import com.oriole.wisepen.market.api.domain.dto.res.ProductInfoResponse;
import com.oriole.wisepen.market.api.enums.ProductStatus;
import com.oriole.wisepen.market.domain.entity.MarketOrderEntity;
import com.oriole.wisepen.market.domain.entity.MarketProductEntity;
import com.oriole.wisepen.market.exception.MarketErrorCode;
import com.oriole.wisepen.market.mapper.MarketOrderMapper;
import com.oriole.wisepen.market.mapper.MarketProductMapper;
import com.oriole.wisepen.market.service.IInfoPointService;
import com.oriole.wisepen.market.service.IMarketService;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import com.oriole.wisepen.resource.domain.dto.req.ResourceUpdateTagsRequest;
import com.oriole.wisepen.resource.enums.ResourceAccessRole;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements IMarketService {

    private final MarketProductMapper marketProductMapper;
    private final MarketOrderMapper marketOrderMapper;
    private final IInfoPointService infoPointService;
    private final RemoteResourceService remoteResourceService;

    @Override
    public PageResult<ProductInfoResponse> getProductList(ProductSearchRequest dto, Integer page, Integer size) {
        // TODO: 关键词搜素/全文搜索
        Page<MarketProductEntity> recordPage = new Page<>(page, size);

        LambdaQueryWrapper<MarketProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MarketProductEntity::getGroupId, dto.getGroupId());
        wrapper.eq(MarketProductEntity::getStatus, ProductStatus.ON_SHELF);
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
        if (product == null) {
            throw new ServiceException(MarketErrorCode.PRODUCT_NOT_FOUND);
        }
        ProductInfoResponse productInfoResponse = new ProductInfoResponse();
        BeanUtil.copyProperties(product, productInfoResponse);
        return productInfoResponse;
    }

    @Override
    public void addProduct(ProductCreateRequest dto) {
        Long currentUserId = SecurityContextHolder.getUserId();
        SecurityContextHolder.assertInGroup(dto.getGroupId());

        // Feign 校验资源所有权
        ResourceCheckPermissionResDTO perm = remoteResourceService.checkResPermission(
                ResourceCheckPermissionReqDTO.builder()
                        .resourceId(dto.getResourceId().toString())
                        .userId(currentUserId)
                        .groupRoles(SecurityContextHolder.getGroupRoleMap())
                        .build()
        ).getData();
        if (perm.getResourceAccessRole() != ResourceAccessRole.OWNER) {
            throw new ServiceException(MarketErrorCode.NOT_RESOURCE_OWNER);
        }

        // 入库（唯一索引）
        MarketProductEntity product = BeanUtil.copyProperties(dto, MarketProductEntity.class);
        product.setSellerId(currentUserId);
        product.setStatus(ProductStatus.ON_SHELF);
        try {
            marketProductMapper.insert(product);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(MarketErrorCode.PRODUCT_ALREADY_LISTED);
        }

        // Feign 挂载资源到集市组 Tag
        ResourceUpdateTagsRequest tagReq = new ResourceUpdateTagsRequest();
        tagReq.setResourceId(dto.getResourceId().toString());
        tagReq.setGroupId(dto.getGroupId().toString());
        tagReq.setTagIds(Collections.singletonList(dto.getTagId().toString()));
        remoteResourceService.updateResourceTags(tagReq);
    }

    @Override
    public void updateProduct(ProductCreateRequest dto) {
        Long currentUserId = SecurityContextHolder.getUserId();
        Long productId = dto.getProductId();
        MarketProductEntity product = marketProductMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException(MarketErrorCode.PRODUCT_NOT_FOUND);
        }
        if (!product.getSellerId().equals(currentUserId)) {
            throw new ServiceException(MarketErrorCode.PRODUCT_PERMISSION_DENIED);
        }

        // 只允许改价格和描述，不允许改交易类型、资源ID等核心字段
        BeanUtil.copyProperties(dto, product, "productId", "sellerId", "resourceId",
                "groupId", "tradeContentType", "ownershipTier", "grantedActions", "createTime");
        marketProductMapper.updateById(product);
    }

    @Override
    public void purchase(Long productId) {
        Long buyerId = SecurityContextHolder.getUserId();

        MarketProductEntity product = marketProductMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException(MarketErrorCode.PRODUCT_NOT_FOUND);
        }

        if (product.getStatus() != ProductStatus.ON_SHELF) {
            throw new ServiceException(MarketErrorCode.PRODUCT_OFF_SHELF);
        }

        if (product.getSellerId() != null && product.getSellerId().equals(buyerId)) {
            throw new ServiceException(MarketErrorCode.CANNOT_BUY_OWN_PRODUCT);
        }

        if (product.getStock() != null && product.getStock() <= 0) {
            throw new ServiceException(MarketErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }

        // 新建订单
        MarketOrderEntity order = MarketOrderEntity.builder()
                .productId(productId)
                .sellerId(product.getSellerId())
                .buyerId(buyerId)
                .price(product.getPrice())
                .build();
        marketOrderMapper.insert(order);

        // 处理订单
        infoPointService.handleTransaction(buyerId, order.getSellerId(), product.getPrice(), order.getOrderId());
    }

    @Override
    public void deleteProduct(Long productId) {
        Long currentUserId = SecurityContextHolder.getUserId();
        MarketProductEntity product = marketProductMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException(MarketErrorCode.PRODUCT_NOT_FOUND);
        }
        if (!product.getSellerId().equals(currentUserId)) {
            throw new ServiceException(MarketErrorCode.PRODUCT_PERMISSION_DENIED);
        }
        product.setStatus(ProductStatus.OFF_SHELF);
        marketProductMapper.updateById(product);

        // Feign 解除集市组 Tag 绑定（tagIds 传空列表 = 清空该组绑定）
        ResourceUpdateTagsRequest tagReq = new ResourceUpdateTagsRequest();
        tagReq.setResourceId(product.getResourceId().toString());
        tagReq.setGroupId(product.getGroupId().toString());
        tagReq.setTagIds(Collections.emptyList());
        remoteResourceService.updateResourceTags(tagReq);
    }

    @Override
    public PageResult<ProductInfoResponse> getMyList(Integer page, Integer size) {
        Long userId = SecurityContextHolder.getUserId();

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

}