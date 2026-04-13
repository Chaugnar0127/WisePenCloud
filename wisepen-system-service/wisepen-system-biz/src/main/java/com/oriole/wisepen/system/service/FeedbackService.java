package com.oriole.wisepen.system.service;

import com.oriole.wisepen.system.api.domain.dto.FeedbackRequest;

/**
 * @author Xiong Heng
 */
public interface FeedbackService {
    void storeFeedback(FeedbackRequest feedbackRequest);
}
