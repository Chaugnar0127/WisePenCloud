package com.oriole.wisepen.media.api.domain.dto.res;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 媒体上传初始化响应。
 */
@Data
public class MediaUploadInitResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 媒体处理 ID，用于后续刷新、重试或取消处理任务。 */
    private String mediaId;

    /** OSS 预签名直传 PUT URL，flashUploaded=true 时为 null。 */
    private String putUrl;

    /** 直传时需附加在 PUT 请求 Header 中的 x-oss-callback 字符串。 */
    private String callbackHeader;

    /** 文件在 OSS 中的 ObjectKey。 */
    private String objectKey;

    /** 是否触发秒传。 */
    private Boolean flashUploaded;
}
