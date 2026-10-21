package com.oriole.wisepen.web.app.repository;

import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageVersionEntity;
import com.oriole.wisepen.web.app.enums.SingleHtmlPageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SingleHtmlPageVersionRepository extends MongoRepository<SingleHtmlPageVersionEntity, String> {

    Optional<SingleHtmlPageVersionEntity> findByResourceId (String resourceId);

    Optional<SingleHtmlPageVersionEntity> findByResourceIdAndVersion(String resourceId, Integer version);

    Page<SingleHtmlPageVersionEntity> findByResourceIdAndStatusOrderByVersionDesc(
            String resourceId, SingleHtmlPageStatus status, Pageable pageable);

    Optional<SingleHtmlPageVersionEntity> findByObjectKey(String objectKey);

    List<SingleHtmlPageVersionEntity> findByUploadMetaUploaderIdAndStatusInOrderByCreateTimeDesc(
            Long uploaderId, List<SingleHtmlPageStatus> statuses);

    List<SingleHtmlPageVersionEntity> findByResourceIdIn(List<String> resourceIds);

    void deleteByResourceIdIn(List<String> resourceIds);
}
