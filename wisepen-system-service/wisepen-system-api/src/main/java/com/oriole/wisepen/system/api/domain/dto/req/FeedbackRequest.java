package com.oriole.wisepen.system.api.domain.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest  {
    private String content;
    private String contact;
    @Size(max = 1024, message = "图片地址长度不能超过1024字符")
    private String imageUrl;
    private String browser;
    @Schema(description = "是否为问题报错", defaultValue = "false")
    private boolean bugReport;
    @Schema(description = "是否为功能建议", defaultValue = "false")
    private boolean suggestion;
    @Schema(description = "是否为使用咨询", defaultValue = "false")
    private boolean consultation;
    @Schema(description = "是否为投诉", defaultValue = "false")
    private boolean complaint;
    @Schema(description = "是否为其他类型", defaultValue = "false")
    private boolean other;

    @AssertTrue(message = "至少选择一种反馈类型")
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isTypeSelected() {
        return bugReport || suggestion || consultation || complaint || other;
    }
}
