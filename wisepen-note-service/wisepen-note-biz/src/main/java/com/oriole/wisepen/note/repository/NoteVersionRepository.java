package com.oriole.wisepen.note.repository;

import com.oriole.wisepen.note.domain.entity.NoteVersionEntity;
import com.oriole.wisepen.note.domain.enums.VersionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteVersionRepository extends MongoRepository<NoteVersionEntity, String> {

    Page<NoteVersionEntity> findByResourceIdOrderByVersionDesc(String resourceId, Pageable pageable);

    Optional<NoteVersionEntity> findByResourceIdAndVersion(String resourceId, Long version);

    /** 查找最近的 FULL 检查点（version <= targetVersion 且 type == FULL） */
    Optional<NoteVersionEntity> findFirstByResourceIdAndTypeAndVersionLessThanEqualOrderByVersionDesc(
            String resourceId, VersionType type, Long version);

    /** 查找检查点到目标版本之间的所有 DELTA */
    List<NoteVersionEntity> findByResourceIdAndVersionBetweenOrderByVersionAsc(
            String resourceId, Long versionStart, Long versionEnd);

    /** 查找指定资源最近的某类型版本 */
    Optional<NoteVersionEntity> findFirstByResourceIdAndTypeOrderByVersionDesc(
            String resourceId, VersionType type);

    /** 查找指定资源在某版本之后的所有指定类型版本 */
    List<NoteVersionEntity> findByResourceIdAndVersionGreaterThanAndTypeOrderByVersionAsc(
            String resourceId, Long version, VersionType type);

    void deleteByResourceId(String resourceId);
}
