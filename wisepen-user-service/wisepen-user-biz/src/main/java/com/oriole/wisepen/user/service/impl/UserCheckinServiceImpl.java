package com.oriole.wisepen.user.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.user.api.config.UserProperties;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointChangeRequest;
import com.oriole.wisepen.user.api.domain.dto.res.UserCheckinResponse;
import com.oriole.wisepen.user.api.domain.dto.res.UserCheckinStatusResponse;
import com.oriole.wisepen.user.api.enums.InfoPointChangeType;
import com.oriole.wisepen.user.domain.entity.UserCheckinEntity;
import com.oriole.wisepen.user.exception.UserError;
import com.oriole.wisepen.user.mapper.UserCheckinMapper;
import com.oriole.wisepen.user.service.IInfoPointWalletService;
import com.oriole.wisepen.user.service.IUserCheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserCheckinServiceImpl implements IUserCheckinService {

    private final UserCheckinMapper userCheckinMapper;
    private final IInfoPointWalletService infoPointWalletService;
    private final UserProperties userProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCheckinResponse checkin(Long userId) {
        LocalDate today = LocalDate.now();
        if (userCheckinMapper.exists(
                Wrappers.<UserCheckinEntity>lambdaQuery()
                        .eq(UserCheckinEntity::getUserId, userId)
                        .eq(UserCheckinEntity::getCheckinDate, today))) {
            throw new ServiceException(UserError.USER_CHECKIN_ALREADY_DONE);
        }

        Integer rewardAmount = userProperties.getCheckin().getRewardAmount();
        UserCheckinEntity checkin = UserCheckinEntity.builder()
                .userId(userId)
                .checkinDate(today)
                .rewardAmount(rewardAmount)
                .build();

        try {
            userCheckinMapper.insert(checkin);
        } catch (DuplicateKeyException ex) {
            throw new ServiceException(UserError.USER_CHECKIN_ALREADY_DONE);
        }

        infoPointWalletService.changeInfoPointBalance(InfoPointChangeRequest.builder()
                .userId(userId)
                .changeAmount(rewardAmount)
                .changeType(InfoPointChangeType.CHECKIN_REWARD)
                .relatedId(checkin.getCheckinId())
                .meta(JSONUtil.toJsonStr(Map.of("checkinDate", today.toString())))
                .operatorId(userId)
                .build());

        return UserCheckinResponse.builder()
                .checkinId(checkin.getCheckinId())
                .checkinDate(today)
                .rewardAmount(rewardAmount)
                .infoPointBalance(infoPointWalletService.getInfoPointBalance(userId))
                .build();
    }

    @Override
    public UserCheckinStatusResponse getTodayStatus(Long userId) {
        LocalDate today = LocalDate.now();
        UserCheckinEntity checkin = userCheckinMapper.selectOne(
                Wrappers.<UserCheckinEntity>lambdaQuery()
                        .eq(UserCheckinEntity::getUserId, userId)
                        .eq(UserCheckinEntity::getCheckinDate, today)
                        .last("limit 1"));

        return UserCheckinStatusResponse.builder()
                .checkedIn(checkin != null)
                .checkinDate(today)
                .rewardAmount(checkin == null ? userProperties.getCheckin().getRewardAmount() : checkin.getRewardAmount())
                .build();
    }
}
