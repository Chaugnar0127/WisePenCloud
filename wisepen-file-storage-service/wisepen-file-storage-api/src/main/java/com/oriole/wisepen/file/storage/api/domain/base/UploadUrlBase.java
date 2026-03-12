package com.oriole.wisepen.file.storage.api.domain.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UploadUrlBase {
    /** 直传的预签名 PUT URL（仅当 flashUploaded=false 时有值） */
    private String putUrl;
    /** 在 PUT 请求时放入 Header 的 x-oss-callback 字符串 */
    private String callbackHeader;
}