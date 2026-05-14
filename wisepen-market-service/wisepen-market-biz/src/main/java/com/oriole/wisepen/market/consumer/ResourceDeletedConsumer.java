package com.oriole.wisepen.market.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.oriole.wisepen.market.api.enums.ProductStatus;
import com.oriole.wisepen.market.domain.entity.MarketProductEntity;
import com.oriole.wisepen.market.mapper.MarketProductMapper;
import com.oriole.wisepen.resource.domain.mq.ResourceDeletedMessage;
import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.oriole.wisepen.resource.constant.MqTopicConstants.TOPIC_RESOURCE_PHYSICAL_DESTROY;

/**
 * 监听资源硬删除事件，级联下架关联商品
 * 当 resource-service 物理删除资源时，market-service 需要把相关商品自动下架
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceDeletedConsumer {

    private final MarketProductMapper marketProductMapper;

    @KafkaListener(
            topics = TOPIC_RESOURCE_PHYSICAL_DESTROY,
            groupId = "wisepen-market-resource-deleted-group"
    )
    public void onResourceDeleted(ResourceDeletedMessage message) {
        Map<ResourceType, List<String>> typedMap = message.getTypedResourceIds();

        // 收集所有被删除的 resourceId
        List<String> allDeletedIds = new ArrayList<>();
        for (List<String> ids : typedMap.values()) {
            if (ids != null) {
                allDeletedIds.addAll(ids);
            }
        }

        if (allDeletedIds.isEmpty()) {
            return;
        }

        // 将 String resourceId 转为 Long 进行查询
        List<Long> resourceIds = new ArrayList<>();
        for (String id : allDeletedIds) {
            try {
                resourceIds.add(Long.parseLong(id));
            } catch (NumberFormatException e) {
                log.warn("无法解析 resourceId={}", id);
            }
        }

        if (resourceIds.isEmpty()) {
            return;
        }

        // 批量下架：将所有关联商品的 status 设为 OFF_SHELF
        LambdaUpdateWrapper<MarketProductEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(MarketProductEntity::getResourceId, resourceIds)
                .eq(MarketProductEntity::getStatus, ProductStatus.ON_SHELF)
                .set(MarketProductEntity::getStatus, ProductStatus.OFF_SHELF);

        int affected = marketProductMapper.update(null, updateWrapper);
        if (affected > 0) {
            log.info("资源删除级联下架: 受影响商品数={}, resourceIds={}", affected, resourceIds);
        }
    }
}
