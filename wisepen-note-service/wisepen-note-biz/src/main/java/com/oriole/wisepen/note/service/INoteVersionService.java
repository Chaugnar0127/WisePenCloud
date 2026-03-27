package com.oriole.wisepen.note.service;

import com.oriole.wisepen.note.api.domain.dto.res.NoteVersionListResponse;
import com.oriole.wisepen.note.domain.enums.VersionType;
import org.springframework.data.domain.Page;

import java.util.List;

public interface INoteVersionService {

    void createVersion(String resourceId, Long version, VersionType type,
                       byte[] data, String label, String userId);

    /** 查询最近一个 FULL 版本之后的所有 DELTA 数据（崩溃恢复用） */
    List<byte[]> findDeltasAfterLatestFull(String resourceId);

    Page<NoteVersionListResponse> listVersions(String resourceId, int page, int size);

    /** 手动保存当前版本为 FULL 检查点 */
    void saveManualVersion(String resourceId, String label, String userId);

    /**
     * 恢复到指定版本，返回重建所需的 FULL 检查点 + DELTA 链的二进制数据。
     * 实际 Yjs 重建由 Node.js 完成。
     */
    byte[][] getVersionChainForRevert(String resourceId, Long targetVersion);

    void deleteByResourceId(String resourceId);
}
