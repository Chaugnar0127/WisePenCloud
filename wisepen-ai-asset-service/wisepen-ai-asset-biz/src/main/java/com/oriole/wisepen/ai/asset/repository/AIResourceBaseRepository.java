package com.oriole.wisepen.ai.asset.repository;

import com.oriole.wisepen.ai.asset.domain.entity.AIResourceBaseEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface AIResourceBaseRepository<T extends AIResourceBaseEntity<T>> extends MongoRepository<T, String> {
    Optional<T> findByResourceId(String resourceId);

    List<T> findByResourceIdInAndVersionGreaterThan(List<String> resourceIds, Integer version);

    void deleteByResourceIdIn(List<String> resourceIds);
}
