package com.oriole.wisepen.ai.asset.repository;

import com.oriole.wisepen.ai.asset.domain.entity.VersionBundleBaseEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface VersionBundleBaseRepository<T extends VersionBundleBaseEntity<T>> extends MongoRepository<T, String> {

    Optional<T> findByResourceIdAndVersion(String resourceId, Integer version);

    List<T> findByResourceId(String resourceId);

    @Query("{ '$or': [ { 'assets.objectKey': ?0 }, { 'assets.pendingUpload.objectKey': ?0 } ] }")
    Optional<T> findFirstByAssetOrPendingObjectKey(String objectKey);

    void deleteByResourceIdIn(List<String> resourceIds);
}
