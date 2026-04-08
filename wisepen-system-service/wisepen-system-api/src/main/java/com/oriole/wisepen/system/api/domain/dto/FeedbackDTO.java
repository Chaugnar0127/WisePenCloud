package com.oriole.wisepen.system.api.domain.dto;

import com.oriole.wisepen.system.api.enums.FeedbackType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Xiong Heng
 */
@Data
@Schema(description = "用户反馈传输对象")
public class FeedbackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "反馈内容", example = "页面点击提交没有反应")
    @NotBlank(message = "反馈内容不能为空")
    private String content;

    @Schema(description = "联系方式（邮箱/手机号）", example = "user@example.com")
    private String contactId;

    @Schema(description = "浏览器型号与版本", example = "Chrome 123.0.0.0")
    private String browser;

    /**
     * 直接使用枚举类型，前端只需传入对应的 code 值（如 1）
     */
    @Schema(description = "反馈类型 (1:问题报错, 2:功能建议, 3:使用咨询, 4:服务投诉, 99:其他)", example = "1")
    @NotNull(message = "反馈类型不能为空")
    private FeedbackType type;
}