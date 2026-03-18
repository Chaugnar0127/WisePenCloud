package com.oriole.wisepen.extension.fudan.feign;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.extension.fudan.domain.dto.FudanUISTaskResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "remoteFudanExtensionService", value = "wisepen-fudan-extension-service", path = "/internal/extenion/fudan")
public interface RemoteFudanExtensionService {

    /**
     * 根据 uid 查询当前 UIS 认证状态
     */
    @GetMapping("/uis/getUISVerificationStatus")
    R<FudanUISTaskResultDTO> getTaskStatus(@RequestParam Long userId);
}