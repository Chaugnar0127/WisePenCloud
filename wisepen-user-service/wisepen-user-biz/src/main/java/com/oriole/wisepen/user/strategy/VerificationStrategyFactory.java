package com.oriole.wisepen.user.strategy;

import com.oriole.wisepen.user.api.enums.UserVerificationMode;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class VerificationStrategyFactory {

    private final Map<UserVerificationMode, UserVerificationStrategy> strategyMap;

    // Spring 会自动注入所有 UserVerificationStrategy 的实现类
    public VerificationStrategyFactory(List<UserVerificationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(UserVerificationStrategy::getMode, Function.identity()));
    }

    /**
     * 根据模式获取对应的策略
     */
    public UserVerificationStrategy getStrategy(UserVerificationMode mode) {
        UserVerificationStrategy strategy = strategyMap.get(mode);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的验证模式: " + mode);
        }
        return strategy;
    }
}