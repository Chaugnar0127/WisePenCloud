package com.oriole.wisepen.note.repository;

import com.oriole.wisepen.note.domain.entity.NoteDocumentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteDocumentRepository extends MongoRepository<NoteDocumentEntity, String> {
    Optional<NoteDocumentEntity> findByResourceId(String resourceId);
    void deleteByResourceId(String resourceId);
}
