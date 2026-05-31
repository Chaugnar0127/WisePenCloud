package com.oriole.wisepen.user.service;

import com.oriole.wisepen.user.api.domain.dto.res.UserCheckinResponse;
import com.oriole.wisepen.user.api.domain.dto.res.UserCheckinStatusResponse;

public interface IUserCheckinService {

    UserCheckinResponse checkin(Long userId);

    UserCheckinStatusResponse getTodayStatus(Long userId);
}
