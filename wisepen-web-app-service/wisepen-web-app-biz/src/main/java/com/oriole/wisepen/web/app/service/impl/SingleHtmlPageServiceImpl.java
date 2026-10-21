package com.oriole.wisepen.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.file.storage.api.feign.RemoteStorageService;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import com.oriole.wisepen.web.app.config.WebAppProperties;
import com.oriole.wisepen.web.app.domain.dto.res.SingleHtmlPageVersionInfoResponse;
import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageEntity;
import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageVersionEntity;
import com.oriole.wisepen.web.app.exception.WebAppError;
import com.oriole.wisepen.web.app.repository.SingleHtmlPageRepository;
import com.oriole.wisepen.web.app.repository.SingleHtmlPageVersionRepository;
import com.oriole.wisepen.web.app.service.ISingleHtmlPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SingleHtmlPageServiceImpl implements ISingleHtmlPageService {

    private final SingleHtmlPageRepository singleHtmlPageRepository;
    private final SingleHtmlPageVersionRepository singleHtmlPageVersionRepository;
    private final RemoteResourceService remoteResourceService;
    private final RemoteStorageService remoteStorageService;
    private final WebAppProperties webAppProperties;

    @Override
    public SingleHtmlPageEntity getSingleHtmlPageInfo(String resourceId) {
        return singleHtmlPageRepository.findById(resourceId)
                .orElseThrow(() -> new ServiceException(WebAppError.SINGLE_HTML_PAGE_NOT_FOUND));
    }

    @Override
    public SingleHtmlPageVersionEntity getSingleHtmlPageVersion(String resourceId, Integer version) {
        SingleHtmlPageEntity page = getSingleHtmlPageInfo(resourceId);
        Integer effectiveVersion = version == null ? page.getCurrentVersion() : version;
        return singleHtmlPageVersionRepository.findByResourceIdAndVersion(resourceId, effectiveVersion)
                .orElseThrow(() -> new ServiceException(WebAppError.SINGLE_HTML_PAGE_VERSION_NOT_FOUND));
    }

    @Override
    public String getSingleHtmlPageContent(String resourceId, Integer version) {
        SingleHtmlPageVersionEntity versionEntity = getSingleHtmlPageVersion(resourceId, version);
        byte[] content;
        try {
            content = remoteStorageService.getSmallFileContent(versionEntity.getObjectKey()).getData();
        } catch (Exception e) {
            log.warn("single HTML page content read failed. resourceId={} version={} objectKey={} dependency=storageService",
                    resourceId, versionEntity.getVersion(), versionEntity.getObjectKey(), e);
            throw new ServiceException(WebAppError.SINGLE_HTML_PAGE_CONTENT_READ_FAILED, e.getMessage());
        }
        if (content == null || !Objects.equals(versionEntity.getContentHash(), DigestUtil.sha256Hex(content))) {
            throw new ServiceException(WebAppError.SINGLE_HTML_PAGE_CONTENT_READ_FAILED);
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    @Override
    public PageR<SingleHtmlPageVersionInfoResponse> listVersions(String resourceId, int page, int size) {
        getSingleHtmlPageInfo(resourceId);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<SingleHtmlPageVersionEntity> entityPage = singleHtmlPageVersionRepository
                .findByResourceIdOrderByVersionDesc(resourceId, pageable);
        PageR<SingleHtmlPageVersionInfoResponse> response = new PageR<>(entityPage.getTotalElements(), page, size);
        response.addAll(entityPage.getContent().stream()
                .map(entity -> BeanUtil.copyProperties(entity, SingleHtmlPageVersionInfoResponse.class))
                .toList());
        return response;
    }

    @Override
    public void deletePages(List<String> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }
        List<String> objectKeys = singleHtmlPageVersionRepository.findByResourceIdIn(resourceIds).stream()
                .map(SingleHtmlPageVersionEntity::getObjectKey)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (!objectKeys.isEmpty()) {
            remoteStorageService.deleteFiles(objectKeys);
        }
        singleHtmlPageVersionRepository.deleteByResourceIdIn(resourceIds);
        singleHtmlPageRepository.deleteByResourceIdIn(resourceIds);
        log.info("single HTML pages deleted. count={} resourceIds={}", resourceIds.size(),
                com.oriole.wisepen.common.core.util.LogIdUtils.summarizeIds(resourceIds));
    }

    private byte[] validateAndEncode(String htmlContent) {
        byte[] htmlBytes = htmlContent.getBytes(StandardCharsets.UTF_8);
        if (htmlBytes.length > webAppProperties.getMaxContentBytes()) {
            throw new ServiceException(WebAppError.SINGLE_HTML_PAGE_CONTENT_TOO_LARGE);
        }
        return htmlBytes;
    }

}
