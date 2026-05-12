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
import com.oriole.wisepen.market.api.enums.OrderStatus;
import com.oriole.wisepen.market.api.enums.ProductStatus;
import com.oriole.wisepen.market.api.enums.TradeType;
import com.oriole.wisepen.market.domain.entity.MarketOrderEntity;
import com.oriole.wisepen.market.domain.entity.MarketProductEntity;
import com.oriole.wisepen.market.exception.MarketErrorCode;
import com.oriole.wisepen.market.mapper.MarketOrderMapper;
import com.oriole.wisepen.market.mapper.MarketProductMapper;
import com.oriole.wisepen.market.service.IInfoPointService;
import com.oriole.wisepen.market.service.IMarketService;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceForkReqDTO;
import com.oriole.wisepen.resource.domain.dto.req.ResourceUpdateActionPermissionRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceUpdateTagsRequest;
import com.oriole.wisepen.resource.enums.OwnershipTier;
import com.oriole.wisepen.resource.enums.ResourceAccessRole;
import com.oriole.wisepen.resource.enums.ResourceAction;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import com.oriole.wisepen.user.api.feign.RemoteUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements IMarketService {

    private final MarketProductMapper marketProductMapper;
    private final MarketOrderMapper marketOrderMapper;
    private final IInfoPointService infoPointService;
    private final RemoteResourceService remoteResourceService;
    private final RemoteUserService remoteUserService;
    private final TransactionTemplate transactionTemplate;

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
                default:
                    wrapper.orderByDesc(MarketProductEntity::getCreateTime);
                    break;
            }
        }

        // 分页查询
        Page<MarketProductEntity> resultPage = marketProductMapper.selectPage(recordPage, wrapper);
        return convertToPageResult(resultPage, page, size);
    }

    @Override
    public ProductInfoResponse getProductDetail(Long productId) {
        MarketProductEntity product = marketProductMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException(MarketErrorCode.PRODUCT_NOT_FOUND);
        }
        ProductInfoResponse response = BeanUtil.copyProperties(product, ProductInfoResponse.class);

        // 填充卖家昵称
        try {
            Map<Long, UserDisplayBase> userMap = remoteUserService
                    .getUserDisplayInfo(Collections.singletonList(product.getSellerId())).getData();
            if (userMap != null && userMap.containsKey(product.getSellerId())) {
                response.setSellerName(userMap.get(product.getSellerId()).getNickname());
            }
        } catch (Exception e) {
            log.warn("获取卖家信息失败, sellerId={}", product.getSellerId());
        }

        // 填充当前用户是否已购买
        Long currentUserId = SecurityContextHolder.getUserId();
        if (currentUserId != null) {
            LambdaQueryWrapper<MarketOrderEntity> orderQuery = new LambdaQueryWrapper<>();
            orderQuery.eq(MarketOrderEntity::getProductId, productId)
                    .eq(MarketOrderEntity::getBuyerId, currentUserId);
            response.setIsPurchased(marketOrderMapper.selectCount(orderQuery) > 0);
        }

        return response;
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

    /**
     * 购买流程（本地事务 + 异步交割）
     * 阶段一（@Transactional 本地事务）：
     *   1. 校验商品状态、库存、自购
     *   2. 创建订单（唯一索引 uk_buyer_product 防重复购买）
     *   3. 扣款/加款（infoPointService.handleTransaction）
     *   4. 更新商品统计（buyerCount + 1）
     *   → 任一步骤异常，整个事务回滚
     * 阶段二（事务提交后，异步 Feign 交割）：
     *   5. 根据 tradeContentType 调用 resource-service 交割权限
     *   → Feign 失败时标记订单为 FAILED，不影响资金安全
     */
    @Override
    public void purchase(Long productId) {
        Long buyerId = SecurityContextHolder.getUserId();

        // 校验
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

        // 阶段一：本地事务（创建订单 + 扣款 + 更新商品统计）
        MarketOrderEntity order = transactionTemplate.execute(status -> {
            // 创建订单（唯一索引 uk_buyer_product 防重复购买）
            MarketOrderEntity newOrder = MarketOrderEntity.builder()
                    .productId(product.getProductId())
                    .sellerId(product.getSellerId())
                    .buyerId(buyerId)
                    .price(product.getPrice())
                    .status(OrderStatus.PAID)
                    .build();
            try {
                marketOrderMapper.insert(newOrder);
            } catch (DuplicateKeyException e) {
                throw new ServiceException(MarketErrorCode.DUPLICATE_PURCHASE);
            }

            // 积分扣款（内部也是 @Transactional，会加入当前事务）
            infoPointService.handleTransaction(buyerId, product.getSellerId(), product.getPrice(), newOrder.getOrderId());

            // 更新商品统计
            product.setBuyerCount(product.getBuyerCount() + 1);
            marketProductMapper.updateById(product);

            return newOrder;
        });

        // 阶段二：异步交割权限（Feign 调用在事务外执行）
        deliverPermission(product, order);
    }

    /**
     * 阶段二：异步权限交割
     * 根据交易类型（使用权/所有权），调用 resource-service 的 Feign 接口
     * 失败时标记订单为 FAILED，等待人工或定时任务重试
     */
    private void deliverPermission(MarketProductEntity product, MarketOrderEntity order) {
        try {
            if (product.getTradeContentType() != null
                    && product.getTradeContentType() == TradeType.OWNERSHIP.getCode()) {
                // 所有权交易：fork 资源给买家
                OwnershipTier tier = OwnershipTier.getByCode(product.getOwnershipTier());

                remoteResourceService.forkRes(ResourceForkReqDTO.builder()
                        .resourceId(product.getResourceId().toString())
                        .newOwnerId(order.getBuyerId().toString())
                        .tier(tier)
                        .build());
            } else {
                // 使用权交易：设置 specifiedUsersGrantedActions
                int grantedMask = (product.getGrantedActions() != null)
                        ? product.getGrantedActions()
                        : ResourceAction.DEFAULT_MEMBER_ACTIONS;

                List<ResourceAction> actions = ResourceAction.permissionCodeToActions(grantedMask);
                Map<String, List<ResourceAction>> specifiedMap = new HashMap<>();
                specifiedMap.put(order.getBuyerId().toString(), actions);

                ResourceUpdateActionPermissionRequest permReq = new ResourceUpdateActionPermissionRequest();
                permReq.setResourceId(product.getResourceId().toString());
                permReq.setSpecifiedUsersGrantedActions(specifiedMap);
                remoteResourceService.updateResourceActionPermission(permReq);
            }

            // 交割成功，更新订单状态
            order.setStatus(OrderStatus.COMPLETED);
            marketOrderMapper.updateById(order);

        } catch (Exception e) {
            // 交割失败，标记订单为 FAILED（资金已到位，权限待补偿）
            log.error("权限交割失败, orderId={}, productId={}, error={}",
                    order.getOrderId(), product.getProductId(), e.getMessage(), e);
            order.setStatus(OrderStatus.FAILED);
            marketOrderMapper.updateById(order);
        }
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
    public void retryDelivery(Long orderId) {
        MarketOrderEntity order = marketOrderMapper.selectById(orderId);
        if (order == null || order.getStatus() != OrderStatus.FAILED) {
            return;
        }
        MarketProductEntity product = marketProductMapper.selectById(order.getProductId());
        if (product == null) {
            log.warn("补偿跳过: 商品已不存在, orderId={}, productId={}", orderId, order.getProductId());
            return;
        }
        
        deliverPermission(product, order);
        log.info("补偿交割完成: orderId={}", orderId);
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
        return convertToPageResult(resultPage, page, size);
    }

    private PageResult<ProductInfoResponse> convertToPageResult(Page<MarketProductEntity> resultPage, Integer page, Integer size) {
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