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
public class NoteOperationLogMessage {
    private String resourceId;
    private List<Entry> entries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {
        private String userId;
        private String operationType;
        /** Base64 编码的 Yjs update 二进制（可选） */
        private String updateData;
        private String contentSummary;
        private Long timestamp;
        private Integer mergedCount;
        /** BlockNote 树状突变详情 */
        private List<Object> details;
    }
}
