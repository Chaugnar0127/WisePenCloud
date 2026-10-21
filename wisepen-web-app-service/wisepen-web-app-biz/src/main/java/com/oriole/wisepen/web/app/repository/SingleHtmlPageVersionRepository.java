package com.oriole.wisepen.web.app.repository;

import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageVersionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SingleHtmlPageVersionRepository extends MongoRepository<SingleHtmlPageVersionEntity, String> {
    Optional<SingleHtmlPageVersionEntity> findByResourceIdAndVersion(String resourceId, Integer version);

    Page<SingleHtmlPageVersionEntity> findByResourceIdOrderByVersionDesc(String resourceId, Pageable pageable);

    List<SingleHtmlPageVersionEntity> findByResourceIdIn(List<String> resourceIds);

    void deleteByResourceIdIn(List<String> resourceIds);
}
