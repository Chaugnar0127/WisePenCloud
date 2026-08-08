package com.oriole.wisepen.note.repository;

import com.oriole.wisepen.note.domain.entity.NoteInfoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteInfoRepository extends MongoRepository<NoteInfoEntity, String> {

    Optional<NoteInfoEntity> findByResourceId(String resourceId);

    List<NoteInfoEntity> findAllByResourceIdIn(List<String> resourceIds);

    void deleteByResourceId(String resourceId);

    void deleteByResourceIdIn(List<String> resourceIds);
}
