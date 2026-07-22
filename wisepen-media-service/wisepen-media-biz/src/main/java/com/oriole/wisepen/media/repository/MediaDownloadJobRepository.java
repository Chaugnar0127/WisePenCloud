package com.oriole.wisepen.media.repository;

import com.oriole.wisepen.media.domain.entity.MediaDownloadJobEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaDownloadJobRepository extends MongoRepository<MediaDownloadJobEntity, String> {

    List<MediaDownloadJobEntity> findByResourceIdIn(List<String> resourceIds);

    void deleteByResourceIdIn(List<String> resourceIds);
}
