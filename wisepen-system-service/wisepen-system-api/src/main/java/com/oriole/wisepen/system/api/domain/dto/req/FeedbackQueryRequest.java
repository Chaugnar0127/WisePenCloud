package com.oriole.wisepen.system.api.domain.dto.req;

import com.oriole.wisepen.system.api.enums.FeedbackStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class FeedbackQueryRequest {
    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int size = 20;

    @Schema(description = "是否筛选问题报错；false 或不传时忽略", defaultValue = "false")
    private boolean bugReport;
    @Schema(description = "是否筛选功能建议；false 或不传时忽略", defaultValue = "false")
    private boolean suggestion;
    @Schema(description = "是否筛选使用咨询；false 或不传时忽略", defaultValue = "false")
    private boolean consultation;
    @Schema(description = "是否筛选投诉；false 或不传时忽略", defaultValue = "false")
    private boolean complaint;
    @Schema(description = "是否筛选其他类型；false 或不传时忽略", defaultValue = "false")
    private boolean other;
    private FeedbackStatus status;

    @Positive
    private Long userId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    @AssertTrue(message = "开始时间不能晚于结束时间")
    @Schema(hidden = true)
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || !startTime.isAfter(endTime);
    }
}
