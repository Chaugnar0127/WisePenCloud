package com.oriole.wisepen.document.util;

import com.oriole.wisepen.document.domain.entity.DocumentPdfMetaEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF 增量更新水印附录构建器（O(1) 预埋模式）。
 *
 * <h3>工作原理</h3>
 * <p>Stage 3 在预览 PDF 中预埋了一个空的 Form XObject（/WisepenWM），
 * 并在每页 Content Stream 末尾追加了 {@code q /WisepenWM Do Q} 调用指令。
 * 预览时，本构建器仅在文件尾部追加 2 个新对象：
 * <ol>
 *   <li>暗水印 Image XObject（128×64 Raw 灰度，固定 8192 字节）</li>
 *   <li>Form XObject（覆盖 preHookObjNum，含明/暗水印绘制指令）</li>
 * </ol>
 * PDF 阅读器加载文件时，XREF 增量段的记录会覆盖旧对象定义，
 * 所有已有的 {@code /WisepenWM Do} 调用均会调用新版本。
 *
 * <h3>O(1) 保证</h3>
 * <ul>
 *   <li>不修改任何 Page Dict 或 Content Stream。</li>
 *   <li>userId 固定长度 → AES 密文固定 16 字节 → Raw 像素固定 8192 字节。</li>
 *   <li>时间戳格式 {@code yyyy-MM-dd HH:mm:ss} 固定 19 字符。</li>
 *   <li>浮点数以 {@code %.3f} 格式输出，位数仅取决于存储的页面尺寸（Stage 3 固定）。</li>
 *   <li>附录大小只与文档的页面尺寸有关，与用户无关，可在 Stage 3 预量。</li>
 * </ul>
 *
 * @author Ian.xiong
 */
public final class WatermarkAppendixBuilder {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 明水印文本中 userId 段的固定字符宽度（不足补空格，超出截断）。public 供 Consumer 读取做 dummy 预量。 */
    public static final int USER_ID_FIELD_WIDTH = 16;

    private WatermarkAppendixBuilder() {
    }

    /**
     * 构建水印增量更新附录字节流。
     *
     * <p>每次调用返回长度恒定（等于 Stage 3 预量的 {@code meta.getAppendixSize()}），
     * 内容随 userId 和 time 变化。
     *
     * @param meta      从 MongoDB 加载的 PDF 结构元数据
     * @param userId    当前用户 ID（不足 {@value #USER_ID_FIELD_WIDTH} 位补空格，超出截断）
     * @param time      水印时间戳（由调用方传入，确保一次请求内明/暗水印时间一致）
     * @param aesKeyB64 AES-128 密钥（Base64），由 DocumentProperties 持有
     * @return 增量更新附录字节，拼接在 originalSize 之后即构成完整的虚拟 PDF
     */
    public static byte[] build(DocumentPdfMetaEntity meta,
                               String userId,
                               LocalDateTime time,
                               String aesKeyB64) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 明水印文字：固定长度，确保 content stream 字节数绝对一致
        String wmText = String.format("%-" + USER_ID_FIELD_WIDTH + "s  %s", userId, time.format(TIME_FMT));

        float pageW = meta.getPages().get(0).getWidthPt();
        float pageH = meta.getPages().get(0).getHeightPt();
        float tw = pageW / 3f;
        float th = pageH / 3f;
        float cx = pageW / 2f;
        float cy = pageH / 2f;

        // 追踪每个新对象相对于原始文件开头的绝对偏移量
        long currentOffset = meta.getOriginalSize();
        List<long[]> xrefEntries = new ArrayList<>(); // [objNum, absoluteOffset]

        // -------------------------------------------------------
        // 对象 1：暗水印 Image XObject（新 ID，不与预埋冲突）
        // -------------------------------------------------------
        int darkImgObjNum = meta.getLastObjectId() + 1;
        xrefEntries.add(new long[]{darkImgObjNum, currentOffset});

        byte[] imgObj = buildImageXObject(darkImgObjNum,
                WatermarkCodec.buildRawTileBytes(userId, aesKeyB64));
        out.write(imgObj);
        currentOffset += imgObj.length;

        // -------------------------------------------------------
        // 对象 2：Form XObject（覆盖预埋占位符，填充真实水印指令）
        // -------------------------------------------------------
        int formObjNum = meta.getPreHookObjNum();
        xrefEntries.add(new long[]{formObjNum, currentOffset});

        byte[] formObj = buildFormXObject(formObjNum, darkImgObjNum,
                wmText, pageW, pageH, tw, th, cx, cy);
        out.write(formObj);
        currentOffset += formObj.length;

        // -------------------------------------------------------
        // XREF 增量段
        // -------------------------------------------------------
        long newXrefOffset = currentOffset;
        out.write(buildXref(xrefEntries));

        // -------------------------------------------------------
        // Trailer（/Size、/Prev、/Root 三项均必须存在）
        // /Root 必须与原始文件的 Catalog 对象编号一致；
        // pdf.js 对此做严格校验，缺少则抛出 "Invalid Root reference."
        // -------------------------------------------------------
        out.write(buildTrailer(darkImgObjNum + 1, meta.getXrefOffset(),
                newXrefOffset, meta.getCatalogObjNum()));

        return out.toByteArray();
    }

    // =========================================================================
    //  私有：对象序列化
    // =========================================================================

    /** 构建 Image XObject 的完整 PDF 对象字节（header + raw pixels + footer）。 */
    private static byte[] buildImageXObject(int objNum, byte[] rawPixels) throws IOException {
        // rawPixels.length 始终为 WatermarkCodec.TILE_W * WatermarkCodec.TILE_H = 8192
        String header = objNum + " 0 obj\n" +
                "<< /Type /XObject /Subtype /Image" +
                " /Width " + WatermarkCodec.TILE_W +
                " /Height " + WatermarkCodec.TILE_H +
                " /ColorSpace /DeviceGray /BitsPerComponent 8" +
                " /Length " + rawPixels.length + " >>\nstream\n";
        String footer = "\nendstream\nendobj\n";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(header.getBytes(StandardCharsets.US_ASCII));
        out.write(rawPixels);
        out.write(footer.getBytes(StandardCharsets.US_ASCII));
        return out.toByteArray();
    }

    /**
     * 构建 Form XObject 的完整 PDF 对象字节。
     * <p>Form 的 BBox 使用第一页尺寸；Resources 内嵌 Font、ExtGState（透明度）、
     * 以及对暗水印 Image 的引用。
     */
    private static byte[] buildFormXObject(int objNum, int darkImgObjNum,
                                            String wmText,
                                            float pageW, float pageH,
                                            float tw, float th,
                                            float cx, float cy) throws IOException {
        String contentStream = buildWatermarkContentStream(wmText, tw, th, cx, cy);
        byte[] csBytes = contentStream.getBytes(StandardCharsets.US_ASCII);

        // 构建 Form 字典头（Resources 包含字体、ExtGState、图像引用）
        String dictHeader = objNum + " 0 obj\n" +
                "<< /Type /XObject /Subtype /Form\n" +
                "   /BBox [0 0 " + ff(pageW) + " " + ff(pageH) + "]\n" +
                "   /Resources <<\n" +
                "     /XObject << /DarkImg " + darkImgObjNum + " 0 R >>\n" +
                "     /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >> >>\n" +
                // GS1: 明水印 25% 透明度；GS2: 暗水印 3% 透明度
                "     /ExtGState << /GS1 << /Type /ExtGState /ca 0.250 /CA 0.250 >>\n" +
                "                   /GS2 << /Type /ExtGState /ca 0.030 /CA 0.030 >> >>\n" +
                "   >>\n" +
                "   /Length " + csBytes.length + "\n" +
                ">>\nstream\n";
        String footer = "\nendstream\nendobj\n";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(dictHeader.getBytes(StandardCharsets.US_ASCII));
        out.write(csBytes);
        out.write(footer.getBytes(StandardCharsets.US_ASCII));
        return out.toByteArray();
    }

    /**
     * 构建 Form XObject 的绘制指令 content stream（纯 ASCII）。
     *
     * <p>结构：
     * <ol>
     *   <li>明水印：GS1（25% 透明度），45° 旋转文字居中。</li>
     *   <li>暗水印：GS2（3% 透明度），DarkImg 3×3 平铺覆盖全页。</li>
     * </ol>
     * 所有浮点数统一格式 {@code %.3f}，字符数恒定。
     */
    private static String buildWatermarkContentStream(String wmText,
                                                       float tw, float th,
                                                       float cx, float cy) {
        StringBuilder sb = new StringBuilder();

        // 明水印：45° 对角文字，25% 透明度
        sb.append("q\n");
        sb.append("/GS1 gs\n");
        sb.append("0.400 g\n");
        sb.append("BT\n");
        sb.append("/F1 14 Tf\n");
        sb.append("0.707 0.707 -0.707 0.707 ").append(ff(cx)).append(' ').append(ff(cy)).append(" Tm\n");
        sb.append('(').append(wmText).append(") Tj\n");
        sb.append("ET\n");
        sb.append("Q\n");

        // 暗水印：3×3 平铺，3% 透明度
        // 每个 tile 用 q...Q 包裹，cm 矩阵不累积
        sb.append("q\n");
        sb.append("/GS2 gs\n");
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                sb.append("q ")
                        .append(ff(tw)).append(" 0 0 ").append(ff(th))
                        .append(' ').append(ff(col * tw)).append(' ').append(ff(row * th))
                        .append(" cm /DarkImg Do Q\n");
            }
        }
        sb.append("Q\n");

        return sb.toString();
    }

    // =========================================================================
    //  私有：XREF + Trailer
    // =========================================================================

    /**
     * 生成 XREF 增量段字节流。
     * <p>每个新/覆写对象单独一个子段（{@code objNum 1\n}），无需连续。
     * 每条 XREF 条目严格 20 字节：
     * {@code NNNNNNNNNN 00000 n \r\n}（10位偏移 + 空格 + 5位代号 + 空格 + n + 空格 + \r\n）。
     */
    private static byte[] buildXref(List<long[]> entries) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("xref\n".getBytes(StandardCharsets.US_ASCII));
        for (long[] entry : entries) {
            out.write((entry[0] + " 1\n").getBytes(StandardCharsets.US_ASCII));
            // 严格 20 字节条目
            out.write(String.format("%010d 00000 n \r\n", entry[1])
                    .getBytes(StandardCharsets.US_ASCII));
        }
        return out.toByteArray();
    }

    /**
     * 生成 Trailer 字节流。
     * <p>PDF 规范（7.5.5 节）要求每个 Trailer 字典均须包含 /Root，
     * 增量更新段不例外；此处直接引用原文件的 Catalog 对象编号。
     *
     * @param catalogObjNum 原始文件 /Root 所指向的 Catalog 对象编号
     */
    private static byte[] buildTrailer(int size, long prevXref, long newXrefOffset, int catalogObjNum) {
        return ("trailer\n<< /Size " + size
                + " /Prev " + prevXref
                + " /Root " + catalogObjNum + " 0 R >>\n"
                + "startxref\n" + newXrefOffset + "\n%%EOF\n")
                .getBytes(StandardCharsets.US_ASCII);
    }

    /** 统一浮点格式（%.3f），保证相同输入始终产生相同字节数。 */
    private static String ff(float v) {
        return String.format("%.3f", v);
    }
}
