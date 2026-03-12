package com.oriole.wisepen.file.storage.api.domain.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StorageRecordBase {
    private String objectKey; // 文件的相对路径
    private String md5; // 文件物理 MD5
    private Long size; // 物理字节大小
}
