package com.oriole.wisepen.web.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wisepen.web-app.single-html-page")
public class WebAppProperties {
    /** 单个 HTML 版本允许的最大 UTF-8 字节数。 */
    private Long maxContentBytes = 2L * 1024 * 1024;
}
