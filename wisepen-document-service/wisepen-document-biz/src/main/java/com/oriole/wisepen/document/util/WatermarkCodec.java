package com.oriole.wisepen.document.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * 暗水印编解码器。
 *
 * <p>算法流程（编码）：
 * <ol>
 *   <li>将 userId 的 UTF-8 字节截断或零填充至 16 字节（AES-128 一个分组大小）。</li>
 *   <li>使用 AES-128-ECB 对该 16 字节进行加密，得到确定性密文（16 字节 = 128 bits）。</li>
 *   <li>将 128 bits 排列为 16×8 二值矩阵（列 = 字节索引，行 = bit 索引，MSB first）。</li>
 *   <li>每个 cell 渲染为 {@value #CELL_PX}×{@value #CELL_PX} 像素的深/浅灰块，生成
 *       {@value #TILE_W}×{@value #TILE_H} 像素的基础 tile。</li>
 *   <li>将 tile 以 3×3 网格平铺到整页，形成 9 份冗余副本。</li>
 *   <li>最终图像以极低透明度（约 2–4%）叠加到 PDF 页面上，肉眼几乎不可见。</li>
 * </ol>
 *
 * <p>算法流程（解码）：
 * <ol>
 *   <li>将页面渲染为 BufferedImage（建议 150 dpi 以保证采样精度）。</li>
 *   <li>将页面划分为 3×3 共 9 个区域，分别提取该区域内的 128 bits。</li>
 *   <li>对应位置的 9 个 bit 做多数投票（≥5 票取 1）以抵抗截图噪声。</li>
 *   <li>AES-128-ECB 解密得到原始 16 字节，去除尾部零填充还原 userId。</li>
 * </ol>
 *
 * <p>抗干扰性：9 份冗余副本 + 多数投票可抵抗截图压缩、局部遮挡等干扰；
 * AES 密文保证无密钥情况下无法伪造。
 */
public final class WatermarkCodec {

    /** 每个 bit cell 的像素边长 */
    private static final int CELL_PX = 8;
    /** 矩阵列数（字节数） */
    private static final int MATRIX_COLS = 16;
    /** 矩阵行数（bit 数/字节） */
    private static final int MATRIX_ROWS = 8;
    /** 基础 tile 像素宽度 */
    public static final int TILE_W = MATRIX_COLS * CELL_PX; // 128
    /** 基础 tile 像素高度 */
    public static final int TILE_H = MATRIX_ROWS * CELL_PX; // 64

    /** bit=1 对应的灰度值（深灰） */
    private static final int GRAY_ONE = 30;
    /** bit=0 对应的灰度值（浅灰） */
    private static final int GRAY_ZERO = 225;
    /** 解码时亮度判断阈值（中点） */
    private static final int DECODE_THRESHOLD = (GRAY_ONE + GRAY_ZERO) / 2; // 127

    private WatermarkCodec() {
    }

    // -------------------------------------------------------------------------
    //  Raw 字节（供 PDF Image XObject 直接嵌入）
    // -------------------------------------------------------------------------

    /**
     * 将 userId 加密后生成 {@value #TILE_W}×{@value #TILE_H} 原始灰度字节数组（始终 8192 字节）。
     * 每字节对应一个像素的灰度值：bit=1 → {@value #GRAY_ONE}，bit=0 → {@value #GRAY_ZERO}。
     * <p>
     * 此格式可直接作为 PDF Image XObject 的 Raw（无压缩）流数据写入文件，
     * 与 PDFBox 的 {@link #buildTile} 输出在视觉上完全一致。
     *
     * @param userId    要嵌入的用户标识
     * @param aesKeyB64 Base64 编码的 AES-128 密钥（同 {@link #buildTile}）
     * @return 固定 {@value #TILE_W} × {@value #TILE_H} = 8192 字节的原始灰度数据
     */
    public static byte[] buildRawTileBytes(String userId, String aesKeyB64) {
        byte[] payload = encrypt(userId, decodeKey(aesKeyB64));
        byte[] raw = new byte[TILE_W * TILE_H];
        for (int byteIdx = 0; byteIdx < MATRIX_COLS; byteIdx++) {
            for (int bitIdx = 0; bitIdx < MATRIX_ROWS; bitIdx++) {
                boolean bit = ((payload[byteIdx] >> (7 - bitIdx)) & 1) == 1;
                byte gray = (byte) (bit ? GRAY_ONE : GRAY_ZERO);
                int baseX = byteIdx * CELL_PX;
                int baseY = bitIdx * CELL_PX;
                for (int py = 0; py < CELL_PX; py++) {
                    for (int px = 0; px < CELL_PX; px++) {
                        raw[(baseY + py) * TILE_W + (baseX + px)] = gray;
                    }
                }
            }
        }
        return raw;
    }

    // -------------------------------------------------------------------------
    //  编码
    // -------------------------------------------------------------------------

    /**
     * 将 userId 加密并渲染为基础 tile（{@value #TILE_W}×{@value #TILE_H} 像素）。
     *
     * @param userId    要嵌入的用户标识，长度不限
     * @param aesKeyB64 Base64 编码的 AES-128 密钥（解码后必须为 16 字节）
     */
    public static BufferedImage buildTile(String userId, String aesKeyB64) {
        byte[] payload = encrypt(userId, decodeKey(aesKeyB64));
        return payloadToTile(payload);
    }

    /**
     * 将基础 tile 以 3×3 网格平铺到目标尺寸，返回整页水印图像（TYPE_ARGB）。
     * 调用方在通过 PDFBox 叠加时应将 GraphicsState 的透明度设置为 0.02–0.04。
     *
     * @param tile        {@link #buildTile} 返回的基础 tile
     * @param pageWidthPx 目标页面像素宽度
     * @param pageHeightPx 目标页面像素高度
     */
    public static BufferedImage tilePage(BufferedImage tile, int pageWidthPx, int pageHeightPx) {
        BufferedImage page = new BufferedImage(pageWidthPx, pageHeightPx, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = page.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int sectionW = pageWidthPx / 3;
        int sectionH = pageHeightPx / 3;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                g.drawImage(tile, col * sectionW, row * sectionH, sectionW, sectionH, null);
            }
        }
        g.dispose();
        return page;
    }

    // -------------------------------------------------------------------------
    //  解码
    // -------------------------------------------------------------------------

    /**
     * 从已渲染的页面图像中恢复 userId。
     *
     * <p>要求页面图像中包含通过 {@link #tilePage} 嵌入的 3×3 暗水印，
     * 且图像宽高可被 3 整除（否则自动向下取整）。
     *
     * @param pageImage 页面渲染图（建议 150 dpi，{@link BufferedImage#TYPE_INT_RGB}）
     * @param aesKeyB64 与编码时相同的 Base64 AES-128 密钥
     * @return 还原的 userId；若解密失败或内容无效则抛出异常
     */
    public static String decode(BufferedImage pageImage, String aesKeyB64) {
        byte[] key = decodeKey(aesKeyB64);
        int imgW = pageImage.getWidth();
        int imgH = pageImage.getHeight();

        int sectionW = imgW / 3;
        int sectionH = imgH / 3;

        // votes[bit] 累计 9 个副本中该 bit 为 1 的票数
        int[] votes = new int[MATRIX_COLS * MATRIX_ROWS];

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int offsetX = col * sectionW;
                int offsetY = row * sectionH;
                extractBitsFromSection(pageImage, offsetX, offsetY, sectionW, sectionH, votes);
            }
        }

        // 多数投票：≥5 票取 1
        byte[] payload = new byte[MATRIX_COLS];
        for (int byteIdx = 0; byteIdx < MATRIX_COLS; byteIdx++) {
            int byteVal = 0;
            for (int bitIdx = 0; bitIdx < MATRIX_ROWS; bitIdx++) {
                int cellIdx = byteIdx * MATRIX_ROWS + bitIdx;
                if (votes[cellIdx] >= 5) {
                    byteVal |= (1 << (7 - bitIdx));
                }
            }
            payload[byteIdx] = (byte) byteVal;
        }

        return decryptToUserId(payload, key);
    }

    // -------------------------------------------------------------------------
    //  内部实现
    // -------------------------------------------------------------------------

    private static BufferedImage payloadToTile(byte[] payload) {
        BufferedImage tile = new BufferedImage(TILE_W, TILE_H, BufferedImage.TYPE_INT_ARGB);
        for (int byteIdx = 0; byteIdx < MATRIX_COLS; byteIdx++) {
            for (int bitIdx = 0; bitIdx < MATRIX_ROWS; bitIdx++) {
                boolean bit = ((payload[byteIdx] >> (7 - bitIdx)) & 1) == 1;
                int gray = bit ? GRAY_ONE : GRAY_ZERO;
                // 完全不透明，透明度由 PDFBox 叠加层控制
                int argb = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
                int baseX = byteIdx * CELL_PX;
                int baseY = bitIdx * CELL_PX;
                for (int px = 0; px < CELL_PX; px++) {
                    for (int py = 0; py < CELL_PX; py++) {
                        tile.setRGB(baseX + px, baseY + py, argb);
                    }
                }
            }
        }
        return tile;
    }

    private static void extractBitsFromSection(
            BufferedImage img,
            int offsetX, int offsetY,
            int sectionW, int sectionH,
            int[] votes) {

        // 每个 cell 在该 section 中的像素大小
        double cellW = (double) sectionW / MATRIX_COLS;
        double cellH = (double) sectionH / MATRIX_ROWS;

        for (int byteIdx = 0; byteIdx < MATRIX_COLS; byteIdx++) {
            for (int bitIdx = 0; bitIdx < MATRIX_ROWS; bitIdx++) {
                int x0 = offsetX + (int) (byteIdx * cellW);
                int y0 = offsetY + (int) (bitIdx * cellH);
                int x1 = offsetX + (int) ((byteIdx + 1) * cellW);
                int y1 = offsetY + (int) ((bitIdx + 1) * cellH);

                // 采样该 cell 中心 2×2 区域的平均亮度，避免边缘噪声
                int cx = (x0 + x1) / 2;
                int cy = (y0 + y1) / 2;
                long sum = 0;
                int count = 0;
                for (int sx = cx - 1; sx <= cx + 1; sx++) {
                    for (int sy = cy - 1; sy <= cy + 1; sy++) {
                        if (sx >= 0 && sx < img.getWidth() && sy >= 0 && sy < img.getHeight()) {
                            int rgb = img.getRGB(sx, sy);
                            // 取绿通道亮度（灰度图 R=G=B）
                            sum += (rgb >> 8) & 0xFF;
                            count++;
                        }
                    }
                }
                int avgBrightness = (count > 0) ? (int) (sum / count) : DECODE_THRESHOLD;
                if (avgBrightness < DECODE_THRESHOLD) {
                    // 亮度低 = 深灰 = bit 1
                    votes[byteIdx * MATRIX_ROWS + bitIdx]++;
                }
            }
        }
    }

    private static byte[] encrypt(String userId, byte[] key) {
        byte[] padded = toPaddedBlock(userId);
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(padded);
        } catch (Exception e) {
            throw new IllegalStateException("暗水印加密失败", e);
        }
    }

    private static String decryptToUserId(byte[] payload, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
            byte[] decrypted = cipher.doFinal(payload);
            // 去除尾部零填充，还原原始字符串
            int len = decrypted.length;
            while (len > 0 && decrypted[len - 1] == 0) {
                len--;
            }
            return new String(decrypted, 0, len, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("暗水印解码失败（密钥不匹配或数据损坏）", e);
        }
    }

    /**
     * 将 userId 的 UTF-8 字节截断或零填充至恰好 16 字节（AES-128 分组大小）。
     * 若 userId 超过 16 字节，取前 16 字节（对超长 ID 可接受的截断策略）。
     */
    private static byte[] toPaddedBlock(String userId) {
        byte[] src = userId.getBytes(StandardCharsets.UTF_8);
        return Arrays.copyOf(src, 16); // 不足补 0，超出截断
    }

    private static byte[] decodeKey(String base64Key) {
        byte[] key = Base64.getDecoder().decode(base64Key);
        if (key.length != 16) {
            // 统一 zero-pad / 截断至 16 字节，保持宽容
            key = Arrays.copyOf(key, 16);
        }
        return key;
    }
}
