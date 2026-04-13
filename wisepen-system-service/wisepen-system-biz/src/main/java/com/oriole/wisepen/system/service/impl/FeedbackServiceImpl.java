package com.oriole.wisepen.system.service.impl;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.system.api.domain.dto.FeedbackRequest;
import com.oriole.wisepen.system.api.enums.FeedbackStatus;
import com.oriole.wisepen.system.domain.entity.Feedback;
import com.oriole.wisepen.system.mapper.FeedbackMapper;
import com.oriole.wisepen.system.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Xiong Heng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void storeFeedback(FeedbackRequest feedbackRequest) {
        Feedback feedback = new Feedback();
        BeanUtils.copyProperties(feedbackRequest, feedback);

        // 业务默认值赋值
        feedback.setStatus(FeedbackStatus.PENDING);

        // 从网关透传的安全上下文中获取当前登录用户ID (未登录时为 null)
        feedback.setUserId(SecurityContextHolder.getUserId());

        feedbackMapper.insert(feedback);
        log.info("成功保存用户反馈, 数据库ID: {}", feedback.getId());
    }
}