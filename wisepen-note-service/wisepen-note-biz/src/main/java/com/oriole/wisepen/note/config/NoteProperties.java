package com.oriole.wisepen.note.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wisepen.note")
public class NoteProperties {
    /** 版本检查点间隔，默认每 10 个版本一个 FULL 检查点 */
    private Integer checkpointInterval = 10;

    /** 操作日志颗粒度：FINE / NORMAL / COARSE */
    private String oplogGranularity = "NORMAL";

    /** 版本保留数量上限（0 表示不限制） */
    private Integer maxVersionsPerNote = 0;

    /** 操作日志保留天数 */
    private Integer oplogRetentionDays = 90;
}
