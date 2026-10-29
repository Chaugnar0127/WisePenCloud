package com.oriole.wisepen.web.app.repository;

import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageVersionEntity;
import com.oriole.wisepen.web.app.enums.SingleHtmlPageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
import java.util.Optional;

public interface SingleHtmlPageVersionRepository extends MongoRepository<SingleHtmlPageVersionEntity, String> {

    Optional<SingleHtmlPageVersionEntity> findByResourceId (String resourceId);

    Optional<SingleHtmlPageVersionEntity> findByResourceIdAndVersion(String resourceId, Integer version);

    Page<SingleHtmlPageVersionEntity> findByResourceIdAndSingleHtmlPageStatusOrderByVersionDesc(
            String resourceId, SingleHtmlPageStatus status, Pageable pageable);

    Optional<SingleHtmlPageVersionEntity> findByObjectKey(String objectKey);

    @Query("{'_id': ?0}")
    @Update("{'$set': {'singleHtmlPageStatus': ?1}}")
    void updateStatusById(String singleHtmlPageId, SingleHtmlPageStatus status);

    @Query("{'_id': ?0}")
    @Update("{'$set': {'resourceId': ?1}}")
    void updateResourceIdById(String singleHtmlPageId, String resourceId);

    List<SingleHtmlPageVersionEntity> findByUploadMetaUploaderIdAndSingleHtmlPageStatusInOrderByCreateTimeDesc(
            Long uploaderId, List<SingleHtmlPageStatus> statuses);

    List<SingleHtmlPageVersionEntity> findByResourceIdIn(List<String> resourceIds);

    void deleteByResourceIdIn(List<String> resourceIds);
}
