package com.oriole.wisepen.document.repository;

import com.oriole.wisepen.document.api.domain.base.DocumentStatus;
import com.oriole.wisepen.document.domain.entity.DocumentInfoEntity;
import com.oriole.wisepen.document.api.enums.DocumentStatusEnum;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentInfoRepository extends MongoRepository<DocumentInfoEntity, String> {

    @Query("{'_id': ?0}")
    @Update("{'$set': {'documentStatus': ?1}}")
    void updateStatusById(String documentId, DocumentStatus status);

    @Query("{'_id': ?0}")
    @Update("{'$set': {'previewObjectKey': ?1}}")
    void updatePreviewObjectKeyById(String documentId, String previewObjectKey);

    @Query("{'_id': ?0}")
    @Update("{'$set': {'resourceId': ?1}}")
    void updateResourceIdById(String documentId, String resourceId);

    @Query("{ 'uploadMeta.uploaderId': ?0, 'documentStatus.status': { $in: ?1 } }")
    List<DocumentInfoEntity> findByUploaderIdAndStatusIn(Long uploaderId, List<DocumentStatusEnum> statusList);

    Optional<DocumentInfoEntity> findByResourceId(String resourceId);

    List<DocumentInfoEntity> findAllByResourceIdIn(List<String> resourceId);

    Optional<DocumentInfoEntity> findBySourceObjectKey(String sourceObjectKey);

    @Query("{ 'documentStatus.status': ?0 }")
    List<DocumentInfoEntity> findByStatus(DocumentStatusEnum status);
}