package com.oriole.wisepen.resource.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 存储服务全局配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "wisepen.resource")
public class ResourceProperties {
    /** 软删除回收站保留天数，默认 30 天 */
    private Integer deletedRetentionDays = 30;

    /** 物理垃圾回收任务 Cron 表达式 */
    private String physicalGcCron = "0 0 3 * * ?";
}