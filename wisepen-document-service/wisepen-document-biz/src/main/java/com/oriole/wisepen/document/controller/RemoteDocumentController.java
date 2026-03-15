package com.oriole.wisepen.document.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.document.domain.entity.DocumentInfoEntity;
import com.oriole.wisepen.document.exception.DocumentErrorCode;
import com.oriole.wisepen.document.mapper.DocumentInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档远程调用接口（供其他微服务通过 Feign 调用，网关应屏蔽外网访问）
 *
 * @author Ian.xiong
 */
@RestController
@RequestMapping("/remote/document")
@RequiredArgsConstructor
public class RemoteDocumentController {

    private final DocumentInfoMapper documentInfoMapper;

    /**
     * 按 documentId（即 resourceId）查询文档基础信息
     */
    @GetMapping("/info/{documentId}")
    public R<DocumentInfoEntity> getDocumentInfo(@PathVariable String documentId) {
        DocumentInfoEntity doc = documentInfoMapper.selectById(documentId);
        if (doc == null) {
            return R.fail(DocumentErrorCode.DOCUMENT_NOT_FOUND);
        }
        return R.ok(doc);
    }
}
