package com.oriole.wisepen.user.api.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 存储服务全局配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "wisepen.user")
public class UserProperties {
    /** 外网能访问到本服务的接口根路径，用于验证邮件 */
    private String apiDomain;

    private String defaultPassword;
}