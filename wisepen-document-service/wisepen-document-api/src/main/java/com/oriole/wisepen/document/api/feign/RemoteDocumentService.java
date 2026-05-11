package com.oriole.wisepen.document.api.feign;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.document.api.domain.dto.DocumentInternalInfoDTO;
import com.oriole.wisepen.document.api.domain.dto.req.DocumentForkReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 内部文档服务 Feign 客户端
 */
@FeignClient(contextId = "remoteDocumentService", value = "wisepen-document-service", path = "/internal/document")
public interface RemoteDocumentService {

    /**
     * 克隆文档信息
     */
    @PostMapping("/forkDocumentInfo")
    R<Void> forkDocumentInfo(@RequestBody DocumentForkReqDTO req);

    /**
     * 获取文档的内部敏感信息（ObjectKeys等）
     */
    @org.springframework.web.bind.annotation.GetMapping("/getInternalDocumentInfo")
    R<DocumentInternalInfoDTO> getInternalDocumentInfo(@org.springframework.web.bind.annotation.RequestParam("resourceId") String resourceId);
}
