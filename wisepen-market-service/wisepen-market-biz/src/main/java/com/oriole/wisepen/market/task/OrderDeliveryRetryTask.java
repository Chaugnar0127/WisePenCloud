package com.oriole.wisepen.market.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oriole.wisepen.market.api.enums.OrderStatus;
import com.oriole.wisepen.market.domain.entity.MarketOrderEntity;
import com.oriole.wisepen.market.mapper.MarketOrderMapper;
import com.oriole.wisepen.market.service.IMarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 交割失败订单补偿定时任务
 * 扫描 status=FAILED 的订单，重新尝试 Feign 权限交割
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDeliveryRetryTask {

    private final MarketOrderMapper marketOrderMapper;
    private final IMarketService marketService;

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
            try {
                marketService.retryDelivery(order.getOrderId());
            } catch (Exception e) {
                log.error("补偿失败: orderId={}, error={}", order.getOrderId(), e.getMessage());
            }
        }
    }
}

