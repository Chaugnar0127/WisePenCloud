package com.oriole.wisepen.document.util;

import java.util.Locale;

public class PdfContentStreamBuilder {
    private final StringBuilder sb = new StringBuilder();

    public PdfContentStreamBuilder saveState() {
        sb.append("q\n");
        return this;
    }

    public PdfContentStreamBuilder restoreState() {
        sb.append("Q\n");
        return this;
    }

    public PdfContentStreamBuilder applyExtGState(String stateName) {
        sb.append(stateName).append(" gs\n");
        return this;
    }

    public PdfContentStreamBuilder setGrayFill(float grayLevel) {
        sb.append(ff(grayLevel)).append(" g\n");
        return this;
    }

    public PdfContentStreamBuilder beginText() {
        sb.append("BT\n");
        return this;
    }

    public PdfContentStreamBuilder endText() {
        sb.append("ET\n");
        return this;
    }

    public PdfContentStreamBuilder setFont(String fontName, int fontSize) {
        sb.append(fontName).append(' ').append(fontSize).append(" Tf\n");
        return this;
    }

    /** 设置文本的仿射变换矩阵 */
    public PdfContentStreamBuilder setTextMatrix(float a, float b, float c, float d, float x, float y) {
        sb.append(ff(a)).append(' ')
                .append(ff(b)).append(' ')
                .append(ff(c)).append(' ')
                .append(ff(d)).append(' ')
                .append(ff(x)).append(' ')
                .append(ff(y)).append(" Tm\n");
        return this;
    }

    /** 绘制文本内容（注意：PDF字符串需用括号包裹） */
    public PdfContentStreamBuilder showText(String text) {
        sb.append('(').append(text).append(") Tj\n");
        return this;
    }

    /** 拼接全局仿射变换矩阵 */
    public PdfContentStreamBuilder concatMatrix(float a, float b, float c, float d, float x, float y) {
        sb.append(ff(a)).append(' ')
                .append(ff(b)).append(' ')
                .append(ff(c)).append(' ')
                .append(ff(d)).append(' ')
                .append(ff(x)).append(' ')
                .append(ff(y)).append(" cm\n");
        return this;
    }

    /** 调用/绘制 XObject（如图片或表单容器） */
    public PdfContentStreamBuilder drawXObject(String xObjectName) {
        sb.append(xObjectName).append(" Do\n");
        return this;
    }

    // 内部私有的高精度浮点数格式化，强制 US 区域避免逗号小数点问题
    private static String ff(float v) {
        return String.format(Locale.US, "%.3f", v);
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}