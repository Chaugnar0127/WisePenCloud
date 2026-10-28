package com.oriole.wisepen.user.service;

import com.oriole.wisepen.user.api.domain.dto.res.UserTourStatusResponse;
import com.oriole.wisepen.user.api.enums.TourCode;

import java.util.List;

public interface IUserTourService {
    List<UserTourStatusResponse> listTourStatus(Long userId);

    boolean isTourCompleted(Long userId, TourCode tourCode);

    void completeTour(Long userId, TourCode tourCode);
}
