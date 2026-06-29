package com.oriole.wisepen.document.service;

import com.oriole.wisepen.document.util.OnlyOfficeConversionClient;
import com.oriole.wisepen.resource.enums.ResourceType;

import java.io.File;

public interface IDocumentFileService {

    // 转换 Office 文件（doc/docx/ppt/pptx/xls/xlsx）
    void convertTo(File source, File target);

    // 转换 Office 文件（doc/docx/ppt/pptx/xls/xlsx）（基于ONLYOFFICE）
    void convertTo(String sourceUrl, String sourceName, ResourceType sourceType, File target, OnlyOfficeConversionClient.ConversionTargetType targetType);

    // 从源文档生成标准 Markdown 内容，用于搜索和 RAG
    String extractMarkdown(File source, ResourceType fileType);

    // 从PDF文件中提取纯文本内容
    String extractPDFText(File file);
}
