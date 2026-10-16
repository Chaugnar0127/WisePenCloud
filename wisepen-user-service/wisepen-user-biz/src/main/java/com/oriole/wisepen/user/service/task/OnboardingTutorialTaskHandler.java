package com.oriole.wisepen.user.service.task;

import com.oriole.wisepen.user.api.config.UserTaskProperties;
import com.oriole.wisepen.user.api.domain.dto.res.UserTaskStatusResponse;
import com.oriole.wisepen.user.api.enums.RewardType;
import com.oriole.wisepen.user.api.enums.UserTaskCode;
import com.oriole.wisepen.user.domain.entity.UserTaskRecordEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class OnboardingTutorialTaskHandler implements UserTaskHandler {

    private static final String META = "完成新手教程";

    private final UserTaskProperties userTaskProperties;

    @Override
    public Set<UserTaskCode> getTaskCodes() {
        return Set.of(UserTaskCode.ONBOARDING_TUTORIAL);
    }

    @Override
    public boolean isEnabled(UserTaskCode taskCode) {
        UserTaskProperties.Rule rule = userTaskProperties.getRules().get(taskCode.getValue());
        return rule != null && Boolean.TRUE.equals(rule.getEnabled());
    }

    @Override
    public UserTaskLimit getLimit(UserTaskCode taskCode) {
        return UserTaskLimit.once();
    }

    @Override
    public UserTaskStatusResponse.UserTaskRewardPreview previewReward(UserTaskCode taskCode) {
        return UserTaskStatusResponse.UserTaskRewardPreview.builder()
                .rewardType(RewardType.NONE)
                .rewardAmount(0)
                .build();
    }

    @Override
    public UserTaskReward calculateReward(Long userId, UserTaskCode taskCode, UserTaskContext context) {
        return UserTaskReward.builder()
                .rewardType(RewardType.NONE)
                .rewardAmount(0)
                .build();
    }

    @Override
    public String buildMeta(Long userId, UserTaskCode taskCode, UserTaskContext context, UserTaskReward reward) {
        return META;
    }

    @Override
    public Object buildCompletedResponse(Long userId, UserTaskCode taskCode, UserTaskContext context,
                                         UserTaskRecordEntity record, UserTaskReward reward) {
        return Boolean.TRUE;
    }

    @Override
    public Object buildSkippedResponse(Long userId, UserTaskCode taskCode, UserTaskContext context,
                                       UserTaskRecordEntity existingRecord) {
        return existingRecord != null;
    }
}
