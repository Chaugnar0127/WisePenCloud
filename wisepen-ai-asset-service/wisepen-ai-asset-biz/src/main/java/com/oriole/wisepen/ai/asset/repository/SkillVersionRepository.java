package com.oriole.wisepen.ai.asset.repository;

import com.oriole.wisepen.ai.asset.domain.entity.SkillVersionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillVersionRepository extends MongoRepository<SkillVersionEntity, String> {
    Optional<SkillVersionEntity> findByResourceIdAndVersion(String resourceId, Integer version);

    List<SkillVersionEntity> findByResourceId(String resourceId);

    @Query("{ '$or': [ { 'mainSkillMD.objectKey': ?0 }, { 'skillAssets.objectKey': ?0 } ] }")
    Optional<SkillVersionEntity> findFirstByAssetObjectKey(String objectKey);

    void deleteByResourceIdIn(List<String> resourceIds);
}
