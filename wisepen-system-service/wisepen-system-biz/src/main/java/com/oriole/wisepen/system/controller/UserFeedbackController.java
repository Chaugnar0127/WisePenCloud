package com.oriole.wisepen.system.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.system.api.domain.dto.FeedbackDTO;
import com.oriole.wisepen.system.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户反馈控制器
 *
 * @author Xiong Heng
 */
@Tag(name = "用户反馈", description = "用户反馈接口")
@RestController
@RequestMapping("/system/feedback")
@RequiredArgsConstructor
public class UserFeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "提交用户反馈", description = "提交问题报错、功能建议、使用咨询等反馈信息")
    @CheckLogin
    @PostMapping("/submit")
    public R<Void> submitFeedback(@Validated @RequestBody FeedbackDTO feedbackDTO) {
        feedbackService.storeFeedback(feedbackDTO);
        return R.ok();
    }
}
