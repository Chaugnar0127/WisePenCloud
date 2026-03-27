package com.oriole.wisepen.note.api.domain.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteSnapshotMessage {
    private String resourceId;
    private Long version;
    /** FULL = 检查点全量, DELTA = 增量 */
    private String type;
    /** Base64 编码的 Yjs 二进制数据 */
    private String data;
    /** FULL 检查点时 Node 提取的纯文本，用于全文检索 */
    private String plainText;
    /** 本轮活跃编辑用户列表 */
    private List<String> updatedBy;
}
