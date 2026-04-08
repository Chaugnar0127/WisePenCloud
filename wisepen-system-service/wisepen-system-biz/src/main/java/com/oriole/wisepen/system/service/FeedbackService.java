package com.oriole.wisepen.system.service;

import com.oriole.wisepen.system.api.domain.dto.FeedbackDTO;

/**
 * @author Xiong Heng
 */
public interface FeedbackService {
    void storeFeedback(FeedbackDTO feedbackDto);
}
