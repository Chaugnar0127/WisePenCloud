package com.oriole.wisepen.note.repository;

import com.oriole.wisepen.note.domain.entity.NoteVersionEntity;
import com.oriole.wisepen.note.api.domain.enums.VersionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteVersionRepository extends MongoRepository<NoteVersionEntity, String> {

    Page<NoteVersionEntity> findByResourceIdOrderByVersionDesc(String resourceId, Pageable pageable);

    /** 查找指定资源最近的某类型版本 */
    Optional<NoteVersionEntity> findFirstByResourceIdAndTypeOrderByVersionDesc(
            String resourceId, VersionType type);

    /** 查找指定资源在目标版本前最近的某类型版本 */
    Optional<NoteVersionEntity> findFirstByResourceIdAndVersionLessThanEqualAndTypeOrderByVersionDesc(
            String resourceId, Long version, VersionType type);

    /** 查找指定资源在某版本之后的所有指定类型版本 */
    List<NoteVersionEntity> findByResourceIdAndVersionGreaterThanAndTypeOrderByVersionAsc(
            String resourceId, Long version, VersionType type);

    /** 查找指定资源在指定版本区间内的所有指定类型版本 */
    @Query("{ 'resourceId': ?0, 'version': { $gt: ?1, $lte: ?2 }, 'type': ?3 }")
    List<NoteVersionEntity> findByResourceIdAndVersionGreaterThanAndVersionLessThanEqualAndTypeOrderByVersionAsc(
            String resourceId, Long fromVersion, Long toVersion, VersionType type);

    void deleteByResourceIdIn(List<String> resourceIds);
}
