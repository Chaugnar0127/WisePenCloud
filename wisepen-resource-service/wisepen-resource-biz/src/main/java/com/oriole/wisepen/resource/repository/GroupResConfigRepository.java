package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.GroupResConfigEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface GroupResConfigRepository extends MongoRepository<GroupResConfigEntity, String> {

    Optional<GroupResConfigEntity> findByGroupId(String groupId);

    void deleteByGroupId(String groupId);
}
