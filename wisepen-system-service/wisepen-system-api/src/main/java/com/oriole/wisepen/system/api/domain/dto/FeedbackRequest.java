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
public class FeedbackRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "反馈内容不能为空")
    private String content;
    private String contact;
    private String browser;
    @NotNull(message = "反馈类型不能为空")
    private FeedbackType type;
}