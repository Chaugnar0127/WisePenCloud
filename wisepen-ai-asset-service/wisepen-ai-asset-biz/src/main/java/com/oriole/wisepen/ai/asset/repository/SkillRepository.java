package com.oriole.wisepen.ai.asset.repository;

import com.oriole.wisepen.ai.asset.domain.entity.SkillEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends MongoRepository<SkillEntity, String> {
    Optional<SkillEntity> findByResourceId(String resourceId);

    void deleteByResourceIdIn(List<String> resourceIds);
}
