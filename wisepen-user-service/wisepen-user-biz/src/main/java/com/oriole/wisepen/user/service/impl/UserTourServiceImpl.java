package com.oriole.wisepen.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.oriole.wisepen.user.api.domain.dto.res.UserTourStatusResponse;
import com.oriole.wisepen.user.api.enums.TourCode;
import com.oriole.wisepen.user.api.enums.UserTaskCode;
import com.oriole.wisepen.user.domain.entity.UserTourRecordEntity;
import com.oriole.wisepen.user.mapper.UserTourRecordMapper;
import com.oriole.wisepen.user.service.IUserTaskService;
import com.oriole.wisepen.user.service.IUserTourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTourServiceImpl implements IUserTourService {
    private final UserTourRecordMapper userTourRecordMapper;
    private final IUserTaskService userTaskService;

    @Override
    public List<UserTourStatusResponse> listTourStatus(Long userId) {
        Set<TourCode> completedTours = userTourRecordMapper.selectList(Wrappers.<UserTourRecordEntity>lambdaQuery()
                        .eq(UserTourRecordEntity::getUserId, userId))
                .stream().map(UserTourRecordEntity::getTourCode).collect(Collectors.toSet());
        return Arrays.stream(TourCode.values())
                .map(tourCode -> UserTourStatusResponse.builder()
                        .tourCode(tourCode).completed(completedTours.contains(tourCode)).build())
                .toList();
    }

    @Override
    public boolean isTourCompleted(Long userId, TourCode tourCode) {
        return userTourRecordMapper.exists(Wrappers.<UserTourRecordEntity>lambdaQuery()
                .eq(UserTourRecordEntity::getUserId, userId)
                .eq(UserTourRecordEntity::getTourCode, tourCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTour(Long userId, TourCode tourCode) {
        if (!isTourCompleted(userId, tourCode)) {
            userTourRecordMapper.insert(UserTourRecordEntity.builder()
                    .userId(userId).tourCode(tourCode).completeTime(LocalDateTime.now()).build());
            log.info("user tour completed. userId={} tourCode={}", userId, tourCode.getValue());
        }
        // 重复汇报也重新判定，允许任务曾停用时完成的 tour 在任务启用后推进任务。
        // 任务处理器已有 ONCE 限制，新增 tour 不会回退或重复完成既有任务。
        if (listTourStatus(userId).stream().allMatch(status -> Boolean.TRUE.equals(status.getCompleted()))) {
            userTaskService.complete(userId, UserTaskCode.ONBOARDING_TUTORIAL, userId, null);
        }
    }
}
