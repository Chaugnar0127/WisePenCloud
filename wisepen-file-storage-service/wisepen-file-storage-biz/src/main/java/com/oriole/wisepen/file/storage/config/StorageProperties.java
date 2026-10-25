package com.oriole.wisepen.file.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 存储服务全局配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "wisepen.storage")
public class StorageProperties {

    private Upload upload = new Upload();
    private Retention retention = new Retention();
    private Endpoint endpoint = new Endpoint();

    @Data
    public static class Upload {
        /** 上传凭证默认有效时长（秒） */
        private Long defaultTicketDuration = 900L;
        /** 小文件最大限制大小（字节） */
        private Long maxSmallFileSize = 5 * 1024 * 1024L;
        /** 上传中记录超时认定时间（小时） */
        private Integer uploadingTimeoutHours = 2;
    }

    @Data
    public static class Retention {
        /** 软删除文件回收站保留天数 */
        private Integer deletedRetentionDays = 30;
    }

    @Data
    public static class Endpoint {
        /** 外网能访问到本服务的接口根路径，用于 OSS 回调 */
        private String apiDomain;
    }
}