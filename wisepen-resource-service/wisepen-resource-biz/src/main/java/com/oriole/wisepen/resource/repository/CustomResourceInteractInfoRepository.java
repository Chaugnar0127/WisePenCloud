package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceInteractInfoEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class CustomResourceInteractInfoRepository {

    private final MongoTemplate mongoTemplate;

    public CustomResourceInteractInfoRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 原子累加资源互动信息表中的阅读量。
     * 使用 upsert：若记录不存在，自动补建并完成自增，无需单独初始化。
     *
     * @param resourceId 资源ID
     * @param delta      本次累加值，通常为 1
     */
    public void incrementReadCount(String resourceId, long delta) {
        Query query = Query.query(Criteria.where("_id").is(resourceId));
        Update update = new Update()
                .inc("readCount", delta)
                .setOnInsert("resourceId", resourceId);
        mongoTemplate.upsert(query, update, ResourceInteractInfoEntity.class);
    }
}
