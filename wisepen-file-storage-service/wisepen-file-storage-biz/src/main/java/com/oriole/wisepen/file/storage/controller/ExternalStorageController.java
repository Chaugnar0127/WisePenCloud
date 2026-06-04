package com.oriole.wisepen.file.storage.controller;

import com.oriole.wisepen.file.storage.service.IStorageService; // 根据你的实际命名调整
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 外部存储服务接口 (直接对外网暴露，处理前端图床直传和云厂商回调)
 */
@Tag(name = "外部文件存储", description = "处理云存储服务回调")
@Slf4j
@RestController
@RequestMapping("/external/storage/")
@RequiredArgsConstructor
public class ExternalStorageController {

    private final IStorageService storageService;

    @Operation(summary = "接收上传完成回调")
    @PostMapping("/callback/upload")
    public String handleUploadCallback(HttpServletRequest request, @RequestBody(required = false) String rawBody) {
        log.info("收到 OSS 回调请求, URI: {}", request.getRequestURI());
        storageService.handleUploadCallback(request, rawBody);
        return "{\"Status\":\"OK\"}";
    }
}
