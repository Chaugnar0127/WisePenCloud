package com.oriole.wisepen.ai.asset.domain.base;

import com.oriole.wisepen.ai.asset.enums.AssetResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 尚未提交的资产上传候选；上传完成前不覆盖 AssetInfoBase 中的正式文件字段。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetPendingUploadBase {
    private String objectKey;
    private AssetResourceType assetResourceType;
    private Long expectedSize;
    private LocalDateTime createTime;
}
