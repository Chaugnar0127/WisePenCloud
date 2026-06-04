package com.oriole.wisepen.system.controller;

import com.oriole.wisepen.system.api.domain.dto.SysOperLogDTO;
import com.oriole.wisepen.system.service.SysOperLogService;
import com.oriole.wisepen.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 远程日志调用接口
 * 供 Feign 内部调用
 */
@Tag(name = "内部日志服务", description = "供其他微服务写入系统操作日志")
@RestController
@RequestMapping("/system/log") // 注意这里的基础路径
public class RemoteLogController {

    @Autowired
    private SysOperLogService sysOperLogService;

    /**
     * 保存日志
     * 对应 RemoteLogService 中的 @PostMapping("/system/log/save")
     */
    @Operation(summary = "内部保存操作日志")
    @PostMapping("/save")
    public R<Boolean> save(@RequestBody SysOperLogDTO dto) {
        return R.ok(sysOperLogService.saveLog(dto));
    }
}
