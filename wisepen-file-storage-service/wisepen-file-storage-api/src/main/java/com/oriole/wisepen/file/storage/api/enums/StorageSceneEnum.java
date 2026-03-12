package com.oriole.wisepen.file.storage.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存储场景枚举：决定文件的物理存放路径与公开级别
 */
@Getter
@AllArgsConstructor
public enum StorageSceneEnum {

    PUBLIC_IMAGE("public/images"),    // 公开图床 (如头像、文章插图，无需鉴权即可访问)
    PRIVATE_IMAGE("private/images"),  // 私密图床 (如群聊图片，需 STS Token 访问)
    PRIVATE_DOC("private/docs");      // 业务文档 (如 PDF、Word，永远在私有域)

    private final String prefix;
}