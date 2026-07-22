package com.oriole.wisepen.media.repository;

import com.oriole.wisepen.media.api.domain.base.MediaStatus;
import com.oriole.wisepen.media.api.enums.ForensicCapability;
import com.oriole.wisepen.media.api.enums.MediaStatusEnum;
import com.oriole.wisepen.media.domain.entity.MediaInfoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaInfoRepository extends MongoRepository<MediaInfoEntity, String> {

    Optional<MediaInfoEntity> findByResourceId(String resourceId);

    List<MediaInfoEntity> findByResourceIdIn(List<String> resourceIds);

    void deleteByResourceIdIn(List<String> resourceIds);

    Optional<MediaInfoEntity> findBySourceObjectKey(String sourceObjectKey);

    @Query("{ 'uploadMeta.uploaderId': ?0, 'mediaStatus.status': { $in: ?1 } }")
    List<MediaInfoEntity> findByUploaderIdAndStatusIn(Long uploaderId, List<MediaStatusEnum> statusList);

    @Query("{'_id': ?0}")
    @Update("{'$set': {'mediaStatus': ?1}}")
    void updateStatusById(String mediaId, MediaStatus status);

    @Query("{'_id': ?0}")
    @Update("{'$set': {'resourceId': ?1}}")
    void updateResourceIdById(String mediaId, String resourceId);

    @Query("{'_id': ?0}")
    @Update("{'$set': {'sourceHlsPrefix': ?1, 'sourceHlsObjectKeys': ?2, 'previewObjectKey': ?3, 'posterObjectKey': ?4, 'durationMs': ?5, 'width': ?6, 'height': ?7}}")
    void updatePackagingResultById(String mediaId,
                                   String sourceHlsPrefix,
                                   List<String> sourceHlsObjectKeys,
                                   String previewObjectKey,
                                   String posterObjectKey,
                                   Long durationMs,
                                   Integer width,
                                   Integer height);

    @Query("{'_id': ?0}")
    @Update("{'$set': {'forensicCapability': ?1}}")
    void updateForensicCapabilityById(String mediaId, ForensicCapability forensicCapability);
}
