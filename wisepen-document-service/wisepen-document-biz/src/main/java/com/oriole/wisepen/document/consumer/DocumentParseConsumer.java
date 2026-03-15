package com.oriole.wisepen.document.consumer;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.document.api.constant.DocumentConstants;
import com.oriole.wisepen.document.api.domain.mq.DocumentParseTaskMessage;
import com.oriole.wisepen.document.api.domain.mq.DocumentReadyMessage;
import com.oriole.wisepen.document.api.enums.DocumentStatusEnum;
import com.oriole.wisepen.document.config.DocumentProperties;
import com.oriole.wisepen.document.domain.entity.DocumentPdfMetaEntity;
import com.oriole.wisepen.document.mq.KafkaDocumentEventPublisher;
import com.oriole.wisepen.document.service.IDocumentParserService;
import com.oriole.wisepen.document.service.IDocumentProcessService;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitReqDTO;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitRespDTO;
import com.oriole.wisepen.file.storage.api.enums.StorageSceneEnum;
import com.oriole.wisepen.file.storage.api.feign.RemoteStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.oriole.wisepen.document.util.WatermarkAppendixBuilder;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oriole.wisepen.document.api.constant.MqTopicConstants.TOPIC_DOCUMENT_PARSE;

/**
 * 文档解析流水线消费者（Stage 3）
 * <p>
 * 消费 {@code wisepen-document-parse-topic} 上的解析任务，执行以下步骤：
 * <ol>
 *   <li>将文档状态推进至 {@code CONVERTING}</li>
 *   <li>通过 storage Feign 获取内网下载 URL，将源文件下载到本地临时目录</li>
 *   <li>Office 文件（doc/ppt/xls 等）经 jodconverter 转换为 PDF；PDF 文件直接使用</li>
 *   <li>使用 PDFBox PDFTextStripper 从 PDF 中提取纯文本内容</li>
 *   <li>向 storage 申请新的预签名直传 URL，后端自身将 PDF 上传至 OSS</li>
 *   <li>检查文档是否已被取消——若是，立即删除刚上传的预览文件（防孤儿对象）</li>
 *   <li>将纯文本写入 MongoDB，获取 textMongoId</li>
 *   <li>发布 {@link DocumentReadyMessage} 到就绪事件 Topic，触发 Stage 4</li>
 * </ol>
 * 任意步骤抛出异常时，文档状态回落为 {@code FAILED}，错误摘要写入 errorMessage 字段，
 * 并清理所有本地临时文件。
 * </p>
 *
 * @author Ian.xiong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentParseConsumer {

    /** 复用单例 HttpClient，用于下载源文件和上传 PDF 预览至 OSS 预签名 URL */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final RemoteStorageService remoteStorageService;
    private final IDocumentParserService documentParserService;
    private final IDocumentProcessService documentProcessService;
    private final KafkaDocumentEventPublisher eventPublisher;
    private final DocumentProperties documentProperties;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC_DOCUMENT_PARSE, groupId = "wisepen-document-parse-group")
    public void onDocumentParse(String payload) {
        DocumentParseTaskMessage msg;
        try {
            msg = objectMapper.readValue(payload, DocumentParseTaskMessage.class);
        } catch (Exception e) {
            log.error("DocumentParseTaskMessage 反序列化失败, payload={}", payload, e);
            return;
        }

        try {
            process(msg);
        } catch (Exception e) {
            log.error("文档解析失败: documentId={}", msg.getDocumentId(), e);
            documentProcessService.markFailed(msg.getDocumentId(), e.getMessage());
        }
    }

    // ==================== 核心流水线 ====================

    private void process(DocumentParseTaskMessage msg) throws Exception {
        // 用户可能在任务派发后取消了文档，提前退出避免浪费下载/转换资源
        if (!documentProcessService.isActive(msg.getDocumentId())) {
            log.info("文档已取消，跳过解析: documentId={}", msg.getDocumentId());
            return;
        }

        documentProcessService.updateStatus(msg.getDocumentId(), DocumentStatusEnum.CONVERTING);

        String downloadUrl = remoteStorageService.getDownloadUrl(msg.getSourceObjectKey(), null).getData();
        String ext = msg.getFileType().getExtension();
        File sourceFile = downloadSourceFile(downloadUrl, msg.getDocumentId(), ext);

        boolean isOffice = DocumentConstants.OFFICE_TYPES.contains(msg.getFileType());
        // Office 文件需要额外的转换临时文件；PDF 文件直接复用 sourceFile 对象
        File pdfFile = isOffice ? createCacheFile(msg.getDocumentId(), ".pdf") : sourceFile;
        // hookedPdf 声明在 try 外，以便 finally 能正确清理
        File hookedPdf = createCacheFile(msg.getDocumentId(), "_hook.pdf");

        try {
            // Office → PDF 格式转换（jodconverter 调用本地 LibreOffice 实例）
            if (isOffice) {
                documentParserService.convertToPdf(sourceFile, pdfFile);
            }

            // 基于 PDF 文件提取纯文本，用于后续建索引（PDFBox PDFTextStripper）
            String rawText = documentParserService.extractText(pdfFile);

            // 预埋空水印占位 Form XObject（/WisepenWM），生成 hooked PDF
            // 上传至 OSS 的是 hooked PDF，而非原始 pdfFile
            embedWatermarkPlaceholder(pdfFile, hookedPdf);

            // 将 hooked PDF 上传至 OSS
            String previewKey = uploadPreviewPdf(msg.getDocumentId(), hookedPdf);

            // 上传预览是耗时操作，期间用户可能已取消文档；若已取消，立即删除刚上传的孤儿预览对象
            if (!documentProcessService.isActive(msg.getDocumentId())) {
                deleteOrphanPreview(previewKey);
                log.info("文档已取消，孤儿预览文件已清理: documentId={}", msg.getDocumentId());
                return;
            }

            // 将纯文本写入 MongoDB，获取 textMongoId 供 Stage 4 回写到 document_info
            String textMongoId = documentProcessService.saveContent(msg.getDocumentId(), rawText);

            // 从 hooked PDF 提取结构元数据（含 preHookObjNum），并预量 appendixSize
            DocumentPdfMetaEntity meta = extractPdfMeta(msg.getDocumentId(), hookedPdf);
            meta.setAppendixSize(measureAppendixSize(meta));
            documentProcessService.savePdfMeta(meta);

            eventPublisher.publishReadyEvent(DocumentReadyMessage.builder()
                    .documentId(msg.getDocumentId())
                    .previewObjectKey(previewKey)
                    .textMongoId(textMongoId)
                    .build());

            log.info("文档解析完成: documentId={}, previewKey={}", msg.getDocumentId(), previewKey);

        } finally {
            deleteSilently(sourceFile);
            if (isOffice) {
                deleteSilently(pdfFile);
            }
            deleteSilently(hookedPdf);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 流式下载源文件到本地缓存目录（使用 Java HttpClient 避免内存中间缓冲）。
     */
    private File downloadSourceFile(String url, String documentId, String ext) throws Exception {
        Path dir = Paths.get(documentProperties.getCachePath());
        Files.createDirectories(dir);
        Path target = dir.resolve(documentId + "_source." + ext);

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofFile(target));
        return target.toFile();
    }

    /**
     * 向 storage 服务申请 PDF 预览文件的预签名直传 URL，然后由本服务后端
     * 自身通过 HTTP PUT 将 PDF 上传至 OSS（携带 OSS 回调 header）。
     *
     * @return previewObjectKey（PDF 在 OSS 中的 ObjectKey）
     */
    private String uploadPreviewPdf(String documentId, File pdfFile) throws Exception {
        UploadInitRespDTO storageData = remoteStorageService.initUpload(
                UploadInitReqDTO.builder()
                        .extension("pdf")
                        .scene(StorageSceneEnum.PRIVATE_DOC)
                        .bizPath(documentId + "_preview")
                        .build()
        ).getData();

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(storageData.getPutUrl()))
                .header("Content-Type", "application/octet-stream")
                .PUT(HttpRequest.BodyPublishers.ofFile(pdfFile.toPath()));
        if (StrUtil.isNotBlank(storageData.getCallbackHeader())) {
            reqBuilder.header("x-oss-callback", storageData.getCallbackHeader());
        }
        HttpResponse<Void> resp = HTTP_CLIENT.send(reqBuilder.build(), HttpResponse.BodyHandlers.discarding());
        if (resp.statusCode() / 100 != 2) {
            throw new IllegalStateException("PDF 上传至 OSS 失败, statusCode=" + resp.statusCode());
        }
        return storageData.getObjectKey();
    }

    /**
     * 删除已上传至 OSS 但因文档取消而成为孤儿的预览文件。
     * 失败时仅记录警告，不应影响取消流程的最终结果。
     */
    private void deleteOrphanPreview(String previewKey) {
        try {
            remoteStorageService.deleteFiles(List.of(previewKey));
        } catch (Exception e) {
            log.warn("孤儿预览文件清理失败，需人工处理: previewKey={}", previewKey, e);
        }
    }

    /**
     * 通过对象身份比对，在 COSDocument 中找到 /WisepenWM Form XObject 的对象编号。
     * <p>
     * 从第一页的 Resources 字典中取出 /WisepenWM 引用的 COSStream 实例，
     * 然后在 XREF 对象表中按 {@code ==} 比对找到匹配的 COSObjectKey。
     */
    private int findPreHookObjNum(PDDocument doc, COSDocument cos) {
        try {
            PDResources res = doc.getPage(0).getResources();
            if (res == null) return 0;
            COSDictionary xobjDict = (COSDictionary) res.getCOSObject()
                    .getDictionaryObject(COSName.XOBJECT);
            if (xobjDict == null) return 0;
            COSBase wmBase = xobjDict.getDictionaryObject(COSName.getPDFName("WisepenWM"));
            if (wmBase == null) return 0;

            for (COSObject obj : cos.getObjectsByType(COSName.XOBJECT)) {
                if (obj.getObject() == wmBase) {
                    return (int) obj.getObjectNumber();
                }
            }
        } catch (Exception e) {
            log.warn("查找 preHookObjNum 失败", e);
        }
        return 0;
    }

    /** 在缓存目录下创建临时文件（用于存放 Office→PDF 转换产物）。 */
    private File createCacheFile(String documentId, String suffix) throws Exception {
        Path dir = Paths.get(documentProperties.getCachePath());
        Files.createDirectories(dir);
        return Files.createTempFile(dir, documentId + "_", suffix).toFile();
    }

    /**
     * 在 PDF 中预埋空的 Form XObject（/WisepenWM），并在每页末尾追加调用指令
     * {@code q /WisepenWM Do Q}。
     * <p>
     * 预埋后的 hooked PDF 是实际上传至 OSS 的文件。预览时增量更新附录覆盖
     * /WisepenWM 的对象定义，注入真实水印内容，无需修改任何 Page Dict。
     *
     * @param source     干净的 PDF 源文件（经过 Office 转换或直接来自上传）
     * @param hookedPdf  输出路径，写入预埋后的 PDF
     */
    private void embedWatermarkPlaceholder(File source, File hookedPdf) throws IOException {
        COSName wmName = COSName.getPDFName("WisepenWM");
        try (PDDocument doc = PDDocument.load(source)) {
            // 创建空 Form XObject（内容仅 "q Q"，作为占位符）
            PDFormXObject emptyForm = new PDFormXObject(doc);
            PDPage firstPage = doc.getPage(0);
            emptyForm.setBBox(firstPage.getMediaBox());
            // PDFormXObject 底层是 COSStream，通过 createOutputStream() 写入内容
            try (OutputStream cs = ((COSStream) emptyForm.getCOSObject()).createOutputStream()) {
                cs.write("q Q\n".getBytes(StandardCharsets.US_ASCII));
            }

            for (PDPage page : doc.getPages()) {
                // 将 /WisepenWM 注册到页面资源
                PDResources resources = page.getResources();
                if (resources == null) {
                    resources = new PDResources();
                    page.setResources(resources);
                }
                resources.put(wmName, emptyForm);

                // 以新 Content Stream 追加 /WisepenWM Do 调用
                PDStream callStream = new PDStream(doc);
                try (OutputStream callCs = callStream.createOutputStream()) {
                    callCs.write("q /WisepenWM Do Q\n".getBytes(StandardCharsets.US_ASCII));
                }
                // 将新 stream 追加到 Contents 数组（兼容原始 Contents 为单 ref 或已是 array）
                COSBase existing = page.getCOSObject().getDictionaryObject(COSName.CONTENTS);
                COSArray contents;
                if (existing instanceof COSArray existingArr) {
                    contents = existingArr;
                } else {
                    contents = new COSArray();
                    if (existing != null) {
                        contents.add(existing);
                    }
                }
                contents.add(callStream.getCOSObject());
                page.getCOSObject().setItem(COSName.CONTENTS, contents);
            }

            doc.save(hookedPdf);
        }
        log.debug("水印占位符预埋完成: source={}, hooked={}", source.getName(), hookedPdf.getName());
    }

    /**
     * 用 dummy 数据跑一遍 {@link WatermarkAppendixBuilder}，量取附录字节数。
     * <p>
     * userId 以全零字符串代替，时间戳取固定未来日期；两者长度与真实数据完全一致，
     * 保证预量结果与运行时生成的附录大小严格相等。
     */
    private long measureAppendixSize(DocumentPdfMetaEntity meta) {
        try {
            // 16 位全零 userId（与真实 userId 等长）
            String dummyUserId = "0".repeat(WatermarkAppendixBuilder.USER_ID_FIELD_WIDTH);
            byte[] dummy = WatermarkAppendixBuilder.build(
                    meta,
                    dummyUserId,
                    LocalDateTime.of(2099, 1, 1, 0, 0, 0),
                    documentProperties.getWatermarkSecretKey()
            );
            return dummy.length;
        } catch (Exception e) {
            log.warn("appendixSize 预量失败，置 0: documentId={}", meta.getDocumentId(), e);
            return 0;
        }
    }

    /**
     * 从本地 PDF 文件中读取 PDF 结构信息，用于后续增量更新式水印附录的预计算。
     * <p>
     * 采集内容：
     * <ul>
     *   <li>{@code originalSize}：文件字节大小</li>
     *   <li>{@code xrefOffset}：最后一个 XREF 段的字节偏移（startxref 值）</li>
     *   <li>{@code lastObjectId}：PDF 中最高的对象编号</li>
     *   <li>每页的对象编号、代号及媒体框尺寸</li>
     * </ul>
     * {@code appendixSize} 在流式预览功能实现后填充，当前置 0。
     */
    private DocumentPdfMetaEntity extractPdfMeta(String documentId, File pdfFile) throws Exception {
        DocumentPdfMetaEntity meta = new DocumentPdfMetaEntity();
        meta.setDocumentId(documentId);
        meta.setOriginalSize(pdfFile.length());

        try (PDDocument doc = PDDocument.load(pdfFile)) {
            COSDocument cos = doc.getDocument();
            meta.setXrefOffset(cos.getStartXref());

            // getHighestXRefObjectNumber() 在 cross-reference stream 格式的 PDF 中
            // 会返回 0，改为遍历所有对象取最大编号以确保可靠性
            int maxObjNum = 0;
            for (COSObject obj : cos.getObjects()) {
                if (obj.getObjectNumber() > maxObjNum) {
                    maxObjNum = (int) obj.getObjectNumber();
                }
            }
            meta.setLastObjectId(maxObjNum);

            // 提取 /Root 对象编号，供增量更新 Trailer 引用（pdf.js 强制要求）
            COSBase rootItem = cos.getTrailer().getItem(COSName.ROOT);
            if (rootItem instanceof COSObject cosRoot) {
                meta.setCatalogObjNum((int) cosRoot.getObjectNumber());
            }

            // 建立 COSDictionary → [objNum, genNum] 的映射，用于定位每页的对象编号
            // PDFBox 2.0.x COSObject 通过 getObjectNumber()/getGenerationNumber() 暴露标识信息
            Map<COSDictionary, long[]> dictToObj = new HashMap<>();
            for (COSObject obj : cos.getObjectsByType(COSName.PAGE)) {
                if (obj.getObject() instanceof COSDictionary dict) {
                    dictToObj.put(dict, new long[]{obj.getObjectNumber(), obj.getGenerationNumber()});
                }
            }

            List<DocumentPdfMetaEntity.PageMeta> pages = new ArrayList<>();
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                PDPage page = doc.getPage(i);
                long[] objInfo = dictToObj.get(page.getCOSObject());
                PDRectangle box = page.getMediaBox();

                DocumentPdfMetaEntity.PageMeta pm = new DocumentPdfMetaEntity.PageMeta();
                pm.setObjNum(objInfo != null ? (int) objInfo[0] : 0);
                pm.setGenNum(objInfo != null ? (int) objInfo[1] : 0);
                pm.setWidthPt(box.getWidth());
                pm.setHeightPt(box.getHeight());
                pages.add(pm);
            }
            meta.setPages(pages);
            meta.setAppendixSize(0); // 由 measureAppendixSize() 覆盖

            // 找出预埋的 /WisepenWM Form XObject 的对象编号
            meta.setPreHookObjNum(findPreHookObjNum(doc, cos));
        }

        log.debug("PDF 元数据提取完成: documentId={}, pages={}, originalSize={}",
                documentId, meta.getPages().size(), meta.getOriginalSize());
        return meta;
    }

    /** 静默删除本地临时文件，失败时仅打印警告，不影响主流程。 */
    private void deleteSilently(File file) {
        if (file != null && file.exists()) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (Exception e) {
                log.warn("临时文件删除失败: {}", file.getAbsolutePath());
            }
        }
    }
}
