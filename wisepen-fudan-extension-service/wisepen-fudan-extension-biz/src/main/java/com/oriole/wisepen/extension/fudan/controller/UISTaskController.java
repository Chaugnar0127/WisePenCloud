package com.oriole.wisepen.extension.fudan.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.extension.fudan.cache.RedisCacheManager;
import com.oriole.wisepen.extension.fudan.domain.dto.FudanUISTaskResultDTO;
import com.oriole.wisepen.extension.fudan.exception.FudanExtensionError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "内部复旦 UIS 认证", description = "供用户服务查询复旦 UIS 认证任务状态")
@RestController
@RequestMapping("/internal/extenion/fudan/uis")
@RequiredArgsConstructor
public class UISTaskController {

    private final RedisCacheManager redisCacheManager;

    @Operation(summary = "内部获取复旦 UIS 认证状态")
    @GetMapping("/getUISVerificationStatus")
    public R<FudanUISTaskResultDTO> getTaskStatus(@RequestParam Long userId) {
        FudanUISTaskResultDTO result = redisCacheManager.getUisTaskStatus(userId);

        if (result == null) {
            throw new ServiceException(FudanExtensionError.UIS_TASK_NOT_FOUND);
        }
        return R.ok(result);
    }
}
