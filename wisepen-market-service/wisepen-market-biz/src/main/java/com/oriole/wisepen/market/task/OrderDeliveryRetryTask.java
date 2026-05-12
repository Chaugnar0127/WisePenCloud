package com.oriole.wisepen.market.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oriole.wisepen.market.api.enums.OrderStatus;
import com.oriole.wisepen.market.api.enums.TradeType;
import com.oriole.wisepen.market.domain.entity.MarketOrderEntity;
import com.oriole.wisepen.market.domain.entity.MarketProductEntity;
import com.oriole.wisepen.market.mapper.MarketOrderMapper;
import com.oriole.wisepen.market.mapper.MarketProductMapper;
import com.oriole.wisepen.resource.domain.dto.ResourceForkReqDTO;
import com.oriole.wisepen.resource.domain.dto.req.ResourceUpdateActionPermissionRequest;
import com.oriole.wisepen.resource.enums.OwnershipTier;
import com.oriole.wisepen.resource.enums.ResourceAction;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交割失败订单补偿定时任务
 * 扫描 status=FAILED 的订单，重新尝试 Feign 权限交割
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDeliveryRetryTask {

    private final MarketOrderMapper marketOrderMapper;
    private final MarketProductMapper marketProductMapper;
    private final RemoteResourceService remoteResourceService;

    /**
     * 每 10 分钟扫描一次 FAILED 订单并重试交割
     */
    @Scheduled(fixedDelayString = "${wisepen.market.delivery-retry-delay-ms:600000}")
    public void retryFailedDelivery() {
        LambdaQueryWrapper<MarketOrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MarketOrderEntity::getStatus, OrderStatus.FAILED);
        List<MarketOrderEntity> failedOrders = marketOrderMapper.selectList(wrapper);

        if (failedOrders.isEmpty()) {
            return;
        }

        log.info("交割补偿任务启动, 待重试订单数={}", failedOrders.size());

        for (MarketOrderEntity order : failedOrders) {
            MarketProductEntity product = marketProductMapper.selectById(order.getProductId());
            if (product == null) {
                log.warn("补偿跳过: 商品已不存在, orderId={}, productId={}",
                        order.getOrderId(), order.getProductId());
                continue;
            }

            try {
                deliverPermission(product, order);
                order.setStatus(OrderStatus.COMPLETED);
                marketOrderMapper.updateById(order);
                log.info("补偿成功: orderId={}", order.getOrderId());
            } catch (Exception e) {
                log.error("补偿失败: orderId={}, error={}", order.getOrderId(), e.getMessage());
            }
        }
    }

    private void deliverPermission(MarketProductEntity product, MarketOrderEntity order) {
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
    }
}
