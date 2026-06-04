package com.oriole.wisepen.document.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@Tag(name = "内部文档服务", description = "供其他微服务调用的文档接口")
@RestController
@RequestMapping("/internal/document")
@RequiredArgsConstructor
public class InternalDocumentController {

}
