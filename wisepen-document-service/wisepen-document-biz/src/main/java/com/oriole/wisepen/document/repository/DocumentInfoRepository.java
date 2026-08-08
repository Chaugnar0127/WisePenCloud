package com.oriole.wisepen.document.repository;

import com.oriole.wisepen.document.domain.entity.DocumentInfoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentInfoRepository extends MongoRepository<DocumentInfoEntity, String> {
    Optional<DocumentInfoEntity> findByResourceId(String resourceId);

    List<DocumentInfoEntity> findAllByResourceIdIn(List<String> resourceIds);

    void deleteByResourceId(String resourceId);

    void deleteByResourceIdIn(List<String> resourceIds);
}
