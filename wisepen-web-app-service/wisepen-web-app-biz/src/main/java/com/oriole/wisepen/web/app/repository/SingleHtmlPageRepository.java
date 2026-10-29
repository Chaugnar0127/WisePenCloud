package com.oriole.wisepen.web.app.repository;

import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SingleHtmlPageRepository extends MongoRepository<SingleHtmlPageEntity, String> {

    Optional<SingleHtmlPageEntity> findByResourceId(String resourceId);

    @Query("{'_id': ?0}")
    @Update("{'$set': {'currentVersion': ?1, 'updateTime': ?2}}")
    void updateVersionByResourceId(String resourceId, Integer version, LocalDateTime updateTime);

    void deleteByResourceIdIn(List<String> resourceIds);
}
