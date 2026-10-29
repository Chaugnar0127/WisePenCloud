package com.oriole.wisepen.web.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Tag(name = "WisePen UI 资源", description = "供 Single HTML Page 使用的版本化 WisePen UI 静态资源")
@RestController
@RequestMapping("/singleHtmlPage/assets/wisepen-ui")
public class WisePenUiAssetController {

    private static final Resource V1_TOKENS = new ClassPathResource("wisepen-ui/v1/tokens.css");
    private static final MediaType CSS_UTF_8 = new MediaType("text", "css", StandardCharsets.UTF_8);
    private static final CacheControl IMMUTABLE_CACHE = CacheControl.maxAge(Duration.ofDays(365))
            .cachePublic()
            .immutable();

    @Operation(
            summary = "获取 WisePen UI v1 样式（内测）",
            description = """
                    - 用途：为 Single HTML Page 提供固定版本的 WisePen UI 设计 token 和组件样式。
                    - 请求：无参数，版本由路径中的 v1 固定指定。
                    - 约束：该资源是公开、不可变的静态样式，不读取用户上下文。
                    - 处理：从 Web App Service classpath 返回 v1/tokens.css，并设置长期公开缓存；不访问数据库或对象存储。
                    - 失败：无业务失败分支。
                    - 响应：返回 text/css;charset=UTF-8 样式正文，缓存一年并标记 immutable。
                    """
    )
    @GetMapping(value = "/v1/tokens.css", produces = "text/css")
    public ResponseEntity<Resource> getV1Tokens() {
        return ResponseEntity.ok()
                .contentType(CSS_UTF_8)
                .cacheControl(IMMUTABLE_CACHE)
                .header("X-Content-Type-Options", "nosniff")
                .body(V1_TOKENS);
    }
}