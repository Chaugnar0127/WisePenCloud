package com.oriole.wisepen.document.controller;

import com.oriole.wisepen.document.api.domain.dto.DocumentInternalInfoDTO;
import lombok.RequiredArgsConstructor;
import com.oriole.wisepen.document.service.IDocumentService;
import org.springframework.web.bind.annotation.*;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.document.api.domain.dto.req.DocumentForkReqDTO;

@RestController
@RequestMapping("/internal/document")
@RequiredArgsConstructor
public class InternalDocumentController {
    private final IDocumentService documentService;

    @PostMapping("/forkDocumentInfo")
    public R<Void> forkDocumentInfo(@RequestBody DocumentForkReqDTO req) {
        documentService.forkDocumentInfo(req);
        return R.ok();
    }

    @GetMapping("/getInternalDocumentInfo")
    public R<DocumentInternalInfoDTO> getInternalDocumentInfo(@RequestParam("resourceId") String resourceId) {
        return R.ok(documentService.getInternalDocumentInfo(resourceId));
    }
}
