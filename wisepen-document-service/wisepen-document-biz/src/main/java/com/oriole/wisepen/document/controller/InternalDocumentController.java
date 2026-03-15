package com.oriole.wisepen.document.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档内部接口（仅供微服务内部调用，网关应屏蔽 /internal/** 外网访问）
 *
 * @author Ian.xiong
 */
@RestController
@RequestMapping("/internal/document")
@RequiredArgsConstructor
public class InternalDocumentController {

}
