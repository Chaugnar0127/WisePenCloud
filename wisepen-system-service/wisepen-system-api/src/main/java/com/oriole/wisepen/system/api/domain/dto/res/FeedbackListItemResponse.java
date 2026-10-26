package com.oriole.wisepen.system.api.domain.dto.res;

import com.oriole.wisepen.system.api.enums.FeedbackStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackListItemResponse {
    @Schema(description = "反馈 ID", requiredMode = Schema.RequiredMode.REQUIRED, type = "string")
    private Long feedbackId;

    @Schema(description = "提交用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, type = "string")
    private Long userId;

    @Schema(description = "是否为问题报错", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean bugReport;

    @Schema(description = "是否为功能建议", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean suggestion;

    @Schema(description = "是否为使用咨询", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean consultation;

    @Schema(description = "是否为投诉", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean complaint;

    @Schema(description = "是否为其他类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean other;

    @Schema(description = "反馈状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private FeedbackStatus status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;
}
