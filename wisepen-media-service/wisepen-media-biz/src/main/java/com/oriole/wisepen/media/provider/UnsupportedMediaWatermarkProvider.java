package com.oriole.wisepen.media.provider;

import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.media.api.enums.ForensicStatus;
import com.oriole.wisepen.media.api.enums.WatermarkSessionStatus;
import com.oriole.wisepen.media.domain.MediaPlaybackGrant;
import com.oriole.wisepen.media.domain.entity.MediaDownloadJobEntity;
import com.oriole.wisepen.media.domain.entity.MediaInfoEntity;
import com.oriole.wisepen.media.domain.entity.MediaWatermarkSessionEntity;
import com.oriole.wisepen.media.exception.MediaError;
import com.oriole.wisepen.resource.enums.ResourceType;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 首期显式不可用的取证水印 Provider。
 */
@Component
@RequiredArgsConstructor
public class UnsupportedMediaWatermarkProvider implements MediaWatermarkProvider {

    @Override
    public MediaPlaybackGrant createPlaybackGrant(MediaInfoEntity mediaInfo,
                                                  MediaWatermarkSessionEntity session) {
        MediaPlaybackGrant.MediaPlaybackGrantBuilder builder = MediaPlaybackGrant.builder()
                .status(WatermarkSessionStatus.READY)
                .deliveryMode(session.getDeliveryMode())
                .forensicStatus(ForensicStatus.UNAVAILABLE);
        if (mediaInfo.getResourceType() == ResourceType.IMAGE) {
            String previewObjectKey = StrUtil.blankToDefault(mediaInfo.getPreviewObjectKey(), mediaInfo.getSourceObjectKey());
            builder.previewObjectKey(previewObjectKey)
                    .deliveryObjectKeys(List.of(previewObjectKey));
        } else if (StrUtil.isNotBlank(mediaInfo.getSourceHlsPrefix())) {
            String manifestObjectKey = mediaInfo.getSourceHlsPrefix() + "/index.m3u8";
            builder.manifestObjectKey(manifestObjectKey)
                    .deliveryObjectKeys(List.of(manifestObjectKey));
        } else {
            builder.status(WatermarkSessionStatus.FAILED);
        }
        return builder.build();
    }

    @Override
    public String createWatermarkDownload(MediaInfoEntity mediaInfo,
                                          MediaWatermarkSessionEntity session,
                                          MediaDownloadJobEntity downloadJob) {
        throw new ServiceException(MediaError.MEDIA_FORENSIC_UNAVAILABLE);
    }
}
