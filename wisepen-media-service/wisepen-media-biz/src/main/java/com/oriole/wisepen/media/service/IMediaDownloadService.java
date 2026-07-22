package com.oriole.wisepen.media.service;

import com.oriole.wisepen.media.api.domain.dto.res.MediaDownloadJobResponse;
import com.oriole.wisepen.media.api.domain.mq.MediaWatermarkDownloadTaskMessage;

public interface IMediaDownloadService {

    MediaDownloadJobResponse createWatermarkDownloadJob(String resourceId, Long requesterId);

    MediaDownloadJobResponse getDownloadJob(String jobId, Long requesterId);

    String getOriginalDownloadUrl(String resourceId);

    void handleWatermarkDownloadTask(MediaWatermarkDownloadTaskMessage message);
}
