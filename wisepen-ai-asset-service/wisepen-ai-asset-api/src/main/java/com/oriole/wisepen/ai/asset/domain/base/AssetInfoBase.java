package com.oriole.wisepen.ai.asset.domain.base;

import com.oriole.wisepen.ai.asset.enums.AssetUploadStatus;
import com.oriole.wisepen.ai.asset.enums.AssetResourceType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class AssetInfoBase {
    private String id;
    private String name;
    private String path;
    private String objectKey;
    private AssetResourceType assetResourceType;
    private AssetUploadStatus uploadStatus;
    private Long size;
    // 替换上传成功前，objectKey、assetResourceType、size 始终表示当前正式文件
    private AssetPendingUploadBase pendingUpload;
}
