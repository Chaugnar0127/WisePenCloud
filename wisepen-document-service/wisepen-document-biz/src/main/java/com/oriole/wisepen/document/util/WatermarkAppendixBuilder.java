package com.oriole.wisepen.document.util;

import com.oriole.wisepen.document.domain.entity.DocumentPdfMetaEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.oriole.wisepen.document.api.constant.DocumentConstants.WATERMARK_SIZE;
import static com.oriole.wisepen.document.util.MicroDotCodec.buildRawTileBytes;

/**
 * PDF 增量更新水印附录构建器（O(1) 预埋模式）。
 *
 * <h3>工作原理</h3>
 * <p>Stage 3 在预览 PDF 中预埋了一个空的 Form XObject（/WisepenWM），
 * 并在每页 Content Stream 末尾追加了 {@code q /WisepenWM Do Q} 调用指令。
 * 预览时，本构建器仅在文件尾部追加 2 个新对象：
 * PDF 阅读器加载文件时，XREF 增量段的记录会覆盖旧对象定义，
 * 所有已有的 {@code /WisepenWM Do} 调用均会调用新版本。
 */
public final class WatermarkAppendixBuilder {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 明水印文本中 userId 段的固定字符宽度。public 供 Consumer 读取做 dummy 预量。 */
    public static final int USER_ID_FIELD_WIDTH = 16;

    private WatermarkAppendixBuilder() {
    }

    /**
     * 构建水印增量更新附录字节流。
     *
     * @param meta      从 MongoDB 加载的 PDF 结构元数据
     * @param userId    当前用户 ID
     * @param time      水印时间戳
     * @param aesKeyB64 AES-128 密钥（Base64）
     * @return 增量更新附录字节，拼接在 originalSize 之后即构成完整的虚拟 PDF
     */
    public static byte[] build(DocumentPdfMetaEntity meta, String userId, LocalDateTime time, String aesKeyB64) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 游标指针
        //所有的新内容只能追加在原文件的最末尾。原文件的大小就是要写入的起始字节位置（Offset）
        long currentOffset = meta.getOriginalSize();

        // XREF 交叉引用表/路由表
        // PDF 是一个支持随机访问的文件格式。阅读器不会从头到尾按顺序读文件，而是查阅文件末尾的 XREF 表，根据对象 ID 直接跳到对应的字节位置
        List<long[]> xrefEntries = new ArrayList<>();

        // -------------------------------------------------------
        // 构建暗水印图片（微型点阵 Image XObject, 128×128 Raw 灰度）
        // -------------------------------------------------------
        // PDF 里的每个元素（页面、字体、图片）都有一个唯一的对象号（Object ID）
        // 为了不和原始文档里的对象冲突，使用原文档最大的对象号+ 1，分配给了我们即将生成的对象
        int darkWMObjNum = meta.getLastObjectId() + 1;

        // 立刻将这个新分配的 darkImgObjNum 和它在文件中的起始物理位置 currentOffset 记录进刚才创建的路由表中
        xrefEntries.add(new long[]{darkWMObjNum, currentOffset});

        // 生成像素点阵（暗水印）
        byte[] drakWM = MicroDotCodec.buildRawTileBytes(userId, aesKeyB64);
        // 封装为 PDF 对象
        byte[] drakWMObj = buildImageXObject(darkWMObjNum, drakWM);
        out.write(drakWMObj); // 写进输出流

        // 游标步进
        // 对象写完必须把消耗掉的字节长度累加到 currentOffset 上
        currentOffset += drakWMObj.length;

        // -------------------------------------------------------
        // 将明水印和暗水印组装为Form XObject，覆盖预埋占位符
        // -------------------------------------------------------

        // 获取占位符 ID
        // Stage 3 生成原始 PDF 时在文件中留下了没有任何实际内容的空对象（占位符），并记录下了它的 ID
        // 每一页的渲染指令里均包含对这个空对象的调用
        int formObjNum = meta.getPreHookObjNum();

        // 重写路由表
        // 直接在增量更新的 XREF 路由表中，强制把这个旧的 formObjNum 映射到当前文件末尾的新物理地址 (currentOffset)
        // 与在面向对象语言中重写虚函数表（vtable）来实现 Hook（钩子）的原理一致，原始页面发出的指令不变，但底层的执行实体已经被热替换了
        xrefEntries.add(new long[]{formObjNum, currentOffset});

        // 生成明水印
        String lightWM = String.format("%-" + USER_ID_FIELD_WIDTH + "s  %s %s", userId, time.format(TIME_FMT), "ACADEMIC USE ONLY");
        // 从文档元数据中读取 PDF 第一页的绝对物理宽度和高度（已假设该 PDF 所有页面的尺寸完全一致）
        float pageW = meta.getPages().getFirst().getWidthPt();
        float pageH = meta.getPages().getFirst().getHeightPt();
        // 计算页面中心点坐标
        float cx = pageW / 2f;
        float cy = pageH / 2f;

        // 封装为 PDF 对象
        // 组装：暗水印对象（darkWM）的ObjNum + 明水印文本（lightWM）
        byte[] formObj = buildFormXObject(formObjNum, darkWMObjNum, lightWM, pageW, pageH, cx, cy);
        out.write(formObj);  // 写进输出流
        currentOffset += formObj.length; // 游标步进

        // -------------------------------------------------------
        // XREF 增量段 + Trailer
        // -------------------------------------------------------

        // 记录“新目录”的物理地址
        long newXrefOffset = currentOffset;
        // 写入增量交叉引用表 XREF
        out.write(buildXref(xrefEntries));

        // 写入文件尾部声明 Trailer
        // Trailer 字典是阅读器打开文件时读取的第一个数据块
        out.write(buildTrailer(darkWMObjNum + 1, meta.getXrefOffset(), newXrefOffset, meta.getCatalogObjNum()));

        // 获取真实生成的有效载荷
        byte[] actualPayload = out.toByteArray();
        if (actualPayload.length > WATERMARK_SIZE) {
            throw new IllegalStateException(
                    String.format("水印附录生成超出定长限制! 实际大小: %d, 最大允许: %d", actualPayload.length, WATERMARK_SIZE));
        }

        // 创建定长块，并使用空格 (ASCII 32) 填满作为 Padding
        byte[] paddedBlock = new byte[WATERMARK_SIZE];
        java.util.Arrays.fill(paddedBlock, (byte) 32);

        // 将真实的 PDF 指令拷贝到块的头部
        System.arraycopy(actualPayload, 0, paddedBlock, 0, actualPayload.length);

        return paddedBlock;
    }

    // 封装 PDF 对象
    // 按照 ISO 32000 (PDF) 标准规范，将一段二进制像素数据封装成一个合法的 PDF 图像对象（Image XObject）
    private static byte[] buildImageXObject(int objNum, byte[] rawPixels) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String header =
                // 声明对象的 ID 和生成号（固定为 0），这是 PDF 交叉引用表（XREF）寻找该对象的锚点
                objNum + " 0 obj\n" +
                // 声明这是一个外部对象（XObject），且具体类型是图像
                "<< /Type /XObject /Subtype /Image" +
                // 声明尺寸与色彩空间
                " /Width " + MicroDotCodec.TILE_SIZE +
                " /Height " + MicroDotCodec.TILE_SIZE +
                " /ColorSpace /DeviceGray /BitsPerComponent 8" +
                // 显式声明数据流的精确字节长度，使得 PDF 解析器在读取时可以直接向前跳跃指定的字节数
                " /Length " + rawPixels.length + " >>\nstream\n";
        out.write(header.getBytes(StandardCharsets.US_ASCII)); // PDF 文件的结构指令在底层规范中必须是 ASCII 编码

        out.write(rawPixels); // 将 rawPixels 字节数组原封不动地写入

        // 按规范闭合数据流（endstream）和对象本身（endobj）
        String footer = "\nendstream\nendobj\n";
        out.write(footer.getBytes(StandardCharsets.US_ASCII));

        return out.toByteArray();
    }

    // 封装 PDF 容器对象 (Form XObject)
    // 按照 ISO 32000 (PDF) 标准规范，生成一个独立、可复用的透明渲染画布容器，包含水印的排版指令及其依赖资源
    private static byte[] buildFormXObject(int objNum, int darkImgObjNum,
                                            String wmText,
                                            float pageW, float pageH,
                                            float cx, float cy) throws IOException {
        String contentStream = buildWatermarkContentStream(wmText, pageW, pageH, cx, cy);
        byte[] csBytes = contentStream.getBytes(StandardCharsets.US_ASCII);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String dictHeader =
                // 声明对象的 ID 和生成号
                objNum + " 0 obj\n" +
                // 声明这是一个外部对象（XObject），具体类型是 Form（可复用的图形容器/画板）
                "<< /Type /XObject /Subtype /Form\n" +
                // 定义画板边界框（BBox），尺寸与物理页面长宽完全一致，超出部分将被裁剪
                "   /BBox [0 0 " + ff(pageW) + " " + ff(pageH) + "]\n" +
                // 声明依赖注入资源字典（告诉解析器如何解析指令流里的别名引用）
                "   /Resources <<\n" +
                // 注入暗水印图片依赖：将指令流里的 /DarkImg 映射到真实生成的图片对象号
                "     /XObject << /DarkImg " + darkImgObjNum + " 0 R >>\n" +
                // 注入字体依赖：定义 /F1 指向系统标准内置的 Helvetica-Bold 字体
                "     /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >> >>\n" +
                // 注入扩展图形状态（用于实现原生 PDF 绘图指令不支持的 Alpha 透明通道）
                // 明水印 25% 透明度
                // 暗水印 5% 透明度
                "     /ExtGState << /GS1 << /Type /ExtGState /ca 0.250 /CA 0.250 >>\n" +
                "                   /GS2 << /Type /ExtGState /ca 0.050 /CA 0.050 >> >>\n" +
                "   >>\n" +
                // 显式声明数据流的精确字节长度，使得 PDF 解析器在读取时可以直接向前跳跃指定的字节数
                "   /Length " + csBytes.length + "\n" +
                ">>\nstream\n";
        out.write(dictHeader.getBytes(StandardCharsets.US_ASCII)); // PDF 文件的结构指令在底层规范中必须是 ASCII 编码

        out.write(csBytes);// 将 绘图指令 字节数组原封不动地写入

        // 按规范闭合数据流（endstream）和对象本身（endobj）
        String footer = "\nendstream\nendobj\n";
        out.write(footer.getBytes(StandardCharsets.US_ASCII));

        return out.toByteArray();
    }

    /**
     * 构建 Form XObject 的绘制指令 content stream。
     *
     * <p>暗水印使用自然步进平铺（无拉伸）：Tile 以 {@link MicroDotCodec#TILE_PT} pt
     * 的物理尺寸在页面上均匀铺设，A4 下约 9×13 = 117 份副本。
     * 由于 TILE_PT 为常量且格式化为 {@code %.3f}，内容流字节数完全由 cols/rows
     * 决定，而 cols/rows 由存储的页面尺寸唯一确定，appendixSize 仍可精确预量。
     */
    private static String buildWatermarkContentStream(String wmText,
                                                       float pageW, float pageH,
                                                       float cx, float cy) {
        PdfContentStreamBuilder builder = new PdfContentStreamBuilder();

        // 估算文本总宽
        float halfTextWidth = wmText.length() * 14 * 0.25f;
        float startX = cx - (halfTextWidth * 0.707f);
        float startY = cy - (halfTextWidth * 0.707f);

        // 明水印：45° 对角文字，25% 透明度
        builder.saveState()
                .applyExtGState("/GS1")
                .setGrayFill(0.400f)
                .beginText()
                .setFont("/F1", 14)
                // 旋转 45 度 (sin45/cos45 约为 0.707) 并平移到中心点 (cx, cy)
                .setTextMatrix(0.707f, 0.707f, -0.707f, 0.707f, startX, startY)
                .showText(wmText)
                .endText()
                .restoreState();

        // 暗水印：自然步进平铺，5% 透明度
        float tilePt = MicroDotCodec.TILE_PT;
        int cols = (int) Math.ceil(pageW / tilePt);
        int rows = (int) Math.ceil(pageH / tilePt);

        builder.saveState()
                .applyExtGState("/GS2");

        // 在二维网格中循环铺设暗水印砖块
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                builder.saveState()
                        // 缩放至 tilePt 大小，并根据行列索引计算物理偏移量
                        .concatMatrix(tilePt, 0, 0, tilePt, col * tilePt, row * tilePt)
                        .drawXObject("/DarkImg")
                        .restoreState();
            }
        }

        builder.restoreState();
        return builder.toString();
    }

    // 重写路由表 XREF
    private static byte[] buildXref(List<long[]> entries) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 声明 XREF 开始
        out.write("xref\n".getBytes(StandardCharsets.US_ASCII));
        for (long[] entry : entries) {
            // 声明：接下来的 1 行，是针对特定对象号（entry[0]）的更新
            out.write((entry[0] + " 1\n").getBytes(StandardCharsets.US_ASCII));
            // 严格按照 PDF 1.7 规范生成的 20 字节定长记录
            // %010d：补零到 10 位的物理偏移量（绝对字节位置）
            // 00000：5 位的生成号（通常是 0）
            // n：代表 “in-use”（该对象正在使用），如果是被删除的对象则用 f (free)。
            // \r\n：尾部固定为 2 字节的回车换行
            out.write(String.format("%010d 00000 n \r\n", entry[1])
                    .getBytes(StandardCharsets.US_ASCII));
        }
        return out.toByteArray();
    }

    // 构建单向链表与文件终点
    private static byte[] buildTrailer(int size, long prevXref, long newXrefOffset, int catalogObjNum) {
        String trailerBlock =
                // 声明 Trailer 开始
                "trailer\n" +
                // Size: 通知阅读器当前文件总共有多少个对象引用（用于初始化内存表）
                "<< /Size " + size +
                // Prev: 通知阅读器前序 XREF（老）的地址
                " /Prev " + prevXref +
                // Root: 指向文档的根节点树（Catalog 对象），通常是 "1 0 R"
                " /Root " + catalogObjNum + " 0 R >>\n" +
                // 声明新目录的入口地址
                "startxref\n" + newXrefOffset +
                // 物理文件的逻辑终点
                "\n%%EOF\n";
        return trailerBlock.getBytes(StandardCharsets.US_ASCII);
    }

    private static String ff(float v) {
        return String.format("%.3f", v);
    }
}
