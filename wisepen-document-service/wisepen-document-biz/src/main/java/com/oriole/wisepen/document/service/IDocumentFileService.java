package com.oriole.wisepen.document.service;

import java.io.File;

public interface IDocumentFileService {

    // 将 Office 文件（doc/docx/ppt/pptx/xls/xlsx）转换为 PDF。
    void convertToPdf(File source, File target);

    // 从PDF文件中提取纯文本内容
    String extractText(File file);
}
