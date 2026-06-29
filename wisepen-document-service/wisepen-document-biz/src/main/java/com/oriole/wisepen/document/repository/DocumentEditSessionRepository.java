package com.oriole.wisepen.document.repository;

import com.oriole.wisepen.document.domain.entity.DocumentEditSessionEntity;
import com.oriole.wisepen.document.api.enums.DocumentEditSessionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface DocumentEditSessionRepository extends MongoRepository<DocumentEditSessionEntity, String> {

    @Query("{ 'resourceId': ?0, 'status': { $in: ?1 }, 'expiresAt': { $gt: ?2 } }")
    Optional<DocumentEditSessionEntity> findActiveByResourceId(String resourceId,
                                                               Collection<DocumentEditSessionStatus> statuses,
                                                               LocalDateTime now);
}
