package com.oriole.wisepen.document.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文档服务配置属性
 *
 * @author Ian.xiong
 */
@Data
@Component
@ConfigurationProperties(prefix = "wisepen.document")
public class DocumentProperties {

    /**
     * 本地临时缓存目录（Stage 3 下载源文件 / 存储转换产物时使用）
     * e.g. /tmp/wisepen/document/cache/
     */
    private String cachePath = "/tmp/wisepen/document/cache/";

    /**
     * 暗水印 AES-128 密钥（Base64 编码，16 字节）。
     * 生产环境必须通过配置中心覆盖此默认值。
     * 默认值仅用于开发/测试，不得用于生产。
     */
    private String watermarkSecretKey = "d2lzZXBlbmRlZmF1bHQ="; // base64("wisependefault") - 14 chars, padded to 16 in codec

    /**
     * 定时任务检测 UPLOADING 文档的执行间隔（毫秒），默认 5 分钟。
     */
    private long staleCheckDelayMs = 300_000L;

    /**
     * 上传超时计算：基础超时时长（毫秒），与文件大小无关的最低等待时间，默认 10 分钟。
     */
    private long baseTimeoutMs = 600_000L;

    /**
     * 上传超时计算：假设的最低上传速度（字节/秒），默认 100 KB/s。
     * timeout = max(baseTimeout, min(maxTimeout, expectedSize / assumedSpeedBps * 1000))
     */
    private long assumedSpeedBps = 102_400L;

    /**
     * 上传超时计算：单文档允许的最大超时时长（毫秒），默认 60 分钟。
     */
    private long maxTimeoutMs = 3_600_000L;
}
