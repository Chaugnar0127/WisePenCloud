package com.oriole.wisepen.file.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 存储服务全局配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "wisepen.storage")
public class StorageProperties {
    /** 上传凭证默认有效时长 (秒)，默认 15 分钟 */
    private Long defaultTicketDuration = 900L;

    /** 小文件最大限制大小，单位：字节 (默认 5MB)*/
    private Long maxSmallFileSize = 5 * 1024 * 1024L;

    /** 死信文件 (UPLOADING) 超时认定时间（小时），默认 2 小时 */
    private Integer uploadingTimeoutHours = 2;

    /** 软删除文件 (DELETED) 回收站保留天数，默认 30 天 */
    private Integer deletedRetentionDays = 30;

    /** 死信清理任务 Cron 表达式 */
    private String zombieCleanupCron = "0 0 */2 * * ?";

    /** 物理垃圾回收任务 Cron 表达式 */
    private String physicalGcCron = "0 0 3 * * ?";

    /** 外网能访问到本服务的接口根路径，用于 OSS 回调 */
    private String apiDomain;
}