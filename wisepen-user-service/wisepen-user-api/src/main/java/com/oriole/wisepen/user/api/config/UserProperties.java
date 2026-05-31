package com.oriole.wisepen.user.api.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 存储服务全局配置属性
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "wisepen.user")
public class UserProperties {
    /** 外网能访问到本服务的接口根路径，用于验证邮件 */
    private String apiDomain;

    private String defaultPassword;

    @Valid
    private Checkin checkin = new Checkin();

    @Data
    public static class Checkin {

        @NotNull
        @Positive
        private Integer rewardAmount;
    }
}
