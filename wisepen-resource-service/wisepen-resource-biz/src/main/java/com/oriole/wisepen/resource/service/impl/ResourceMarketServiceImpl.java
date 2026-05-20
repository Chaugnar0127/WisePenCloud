package com.oriole.wisepen.resource.service.impl;

import cn.hutool.core.util.IdUtil;
import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import com.oriole.wisepen.note.api.feign.RemoteNoteService;
import com.oriole.wisepen.resource.domain.ResourceSellInfo;
import com.oriole.wisepen.resource.domain.SellReviewInfo;
import com.oriole.wisepen.resource.domain.dto.req.ResourceMarketQueryRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourcePublishSellRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourcePurchaseRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceReviewSellRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceSubscriptionForkRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceUpdateSellRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceUpdateTagsRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceMarketDetailResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceMarketItemResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourcePurchaseResponse;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.enums.SaleMethod;
import com.oriole.wisepen.resource.exception.ResourceError;
import com.oriole.wisepen.resource.repository.ResourceItemRepository;
import com.oriole.wisepen.resource.service.IResourceMarketService;
import com.oriole.wisepen.resource.service.IResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceMarketServiceImpl implements IResourceMarketService {

    private final ResourceItemRepository resourceItemRepository;
    private final IResourceService resourceService;
    private final RemoteNoteService remoteNoteService;

    @Override
    public PageR<ResourceMarketItemResponse> listMarketResources(ResourceMarketQueryRequest req, int page, int size) {
        throw new ServiceException(ResourceError.RESOURCE_MARKET_OPERATION_UNSUPPORTED);
    }

    @Override
    public ResourceMarketDetailResponse getMarketDetail(String resourceId) {
        throw new ServiceException(ResourceError.RESOURCE_MARKET_OPERATION_UNSUPPORTED);
    }

    @Override
    public void publishSellInfo(ResourcePublishSellRequest req) {
        String currentUserId = SecurityContextHolder.getUserId().toString();
        ResourceItemEntity resource = getResource(req.getResourceId());

        assertOwner(resource, currentUserId);
        SecurityContextHolder.assertInGroup(Long.valueOf(req.getGroupId()));

        if (isResell(req.getSaleMethod())) {
            assertResellAllowed(resource);
        }
        assertNoActiveSameSale(resource, req.getGroupId(), req.getSaleMethod());

        ResourceSellInfo sellInfo = ResourceSellInfo.builder()
                .sellId(IdUtil.fastSimpleUUID())
                .groupId(req.getGroupId())
                .tagId(req.getTagId())
                .price(req.getPrice())
                .saleMethod(req.getSaleMethod())
                .previewType(req.getPreviewType())
                .version(resolveListedVersion(resource))
                .offShelf(false)
                .listedAt(LocalDateTime.now())
                .admin(SellReviewInfo.builder().approved(null).build())
                .build();

        List<ResourceSellInfo> sellInfos = mutableSellInfos(resource);
        sellInfos.add(sellInfo);
        resourceItemRepository.save(resource);

        bindMarketTag(resource.getResourceId(), sellInfo);
        log.info("resource sellInfo published resourceId={} sellId={} groupId={} saleMethod={}",
                resource.getResourceId(), sellInfo.getSellId(), sellInfo.getGroupId(), sellInfo.getSaleMethod());
    }

    @Override
    public void updateSellInfo(ResourceUpdateSellRequest req) {
        String currentUserId = SecurityContextHolder.getUserId().toString();
        ResourceItemEntity resource = getResource(req.getResourceId());

        assertOwner(resource, currentUserId);
        ResourceSellInfo sellInfo = findSellInfo(resource, req.getSellId());

        if (req.getPrice() != null) {
            sellInfo.setPrice(req.getPrice());
        }
        if (req.getPreviewType() != null) {
            sellInfo.setPreviewType(req.getPreviewType());
        }
        if (req.getTagId() != null) {
            sellInfo.setTagId(req.getTagId());
            bindMarketTag(resource.getResourceId(), sellInfo);
        }

        resourceItemRepository.save(resource);
        log.info("resource sellInfo updated resourceId={} sellId={}", resource.getResourceId(), sellInfo.getSellId());
    }

    @Override
    public void offShelfSellInfo(String resourceId, String sellId) {
        String currentUserId = SecurityContextHolder.getUserId().toString();
        ResourceItemEntity resource = getResource(resourceId);

        assertOwner(resource, currentUserId);
        ResourceSellInfo sellInfo = findSellInfo(resource, sellId);
        sellInfo.setOffShelf(true);

        resourceItemRepository.save(resource);
        log.info("resource sellInfo offShelf resourceId={} sellId={}", resource.getResourceId(), sellInfo.getSellId());
    }

    @Override
    public void reviewSellInfo(ResourceReviewSellRequest req) {
        Long reviewerId = SecurityContextHolder.getUserId();
        ResourceItemEntity resource = getResource(req.getResourceId());
        ResourceSellInfo sellInfo = findSellInfo(resource, req.getSellId());

        SecurityContextHolder.assertGroupRole(Long.valueOf(sellInfo.getGroupId()), GroupRoleType.ADMIN, GroupRoleType.OWNER);

        SellReviewInfo reviewInfo = sellInfo.getAdmin();
        if (reviewInfo == null) {
            reviewInfo = new SellReviewInfo();
            sellInfo.setAdmin(reviewInfo);
        }
        reviewInfo.setApproved(req.getApproved());
        reviewInfo.setComment(req.getComment());
        reviewInfo.setReviewerId(reviewerId.toString());
        reviewInfo.setReviewedAt(LocalDateTime.now());

        resourceItemRepository.save(resource);
        log.info("resource sellInfo reviewed resourceId={} sellId={} approved={}",
                resource.getResourceId(), sellInfo.getSellId(), req.getApproved());
    }

    @Override
    public ResourcePurchaseResponse purchase(ResourcePurchaseRequest req) {
        throw new ServiceException(ResourceError.RESOURCE_MARKET_OPERATION_UNSUPPORTED);
    }

    @Override
    public String forkLatestBySubscription(ResourceSubscriptionForkRequest req) {
        throw new ServiceException(ResourceError.RESOURCE_MARKET_OPERATION_UNSUPPORTED);
    }

    private ResourceItemEntity getResource(String resourceId) {
        return resourceItemRepository.findById(resourceId)
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));
    }

    private void assertOwner(ResourceItemEntity resource, String userId) {
        if (!userId.equals(resource.getOwnerId())) {
            throw new ServiceException(ResourceError.RESOURCE_PERMISSION_DENIED);
        }
    }

    private boolean isResell(SaleMethod saleMethod) {
        return saleMethod == SaleMethod.OWNERSHIP || saleMethod == SaleMethod.SUBSCRIPTION;
    }

    private void assertResellAllowed(ResourceItemEntity resource) {
        List<String> originalEditorIds = resource.getOriginalEditorIds();
        if (originalEditorIds == null || !originalEditorIds.contains(resource.getOwnerId())) {
            throw new ServiceException(ResourceError.RESOURCE_RESELL_NOT_ALLOWED);
        }
    }

    private void assertNoActiveSameSale(ResourceItemEntity resource, String groupId, SaleMethod saleMethod) {
        boolean exists = safeSellInfos(resource).stream()
                .anyMatch(sellInfo -> Objects.equals(groupId, sellInfo.getGroupId())
                        && sellInfo.getSaleMethod() == saleMethod
                        && !Boolean.TRUE.equals(sellInfo.getOffShelf()));
        if (exists) {
            throw new ServiceException(ResourceError.SELL_INFO_ALREADY_LISTED);
        }
    }

    private ResourceSellInfo findSellInfo(ResourceItemEntity resource, String sellId) {
        return safeSellInfos(resource).stream()
                .filter(sellInfo -> Objects.equals(sellInfo.getSellId(), sellId))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ResourceError.SELL_INFO_NOT_FOUND));
    }

    private List<ResourceSellInfo> mutableSellInfos(ResourceItemEntity resource) {
        if (resource.getSellInfos() == null) {
            resource.setSellInfos(new ArrayList<>());
        }
        return resource.getSellInfos();
    }

    private List<ResourceSellInfo> safeSellInfos(ResourceItemEntity resource) {
        return resource.getSellInfos() == null ? Collections.emptyList() : resource.getSellInfos();
    }

    private Long resolveListedVersion(ResourceItemEntity resource) {
        ResourceType resourceType = resource.getResourceType();
        if (resourceType == ResourceType.NOTE) {
            R<NoteSnapshotResponse> response = remoteNoteService.getNoteLatestVersion(resource.getResourceId());
            NoteSnapshotResponse snapshot = response == null ? null : response.getData();
            return snapshot == null || snapshot.getVersion() == null ? 0L : snapshot.getVersion();
        }
        if (resourceType != null && resourceType.isOffice()) {
            return 0L;
        }
        throw new ServiceException(ResourceError.RESOURCE_TYPE_UNSUPPORTED_FOR_SELL);
    }

    private void bindMarketTag(String resourceId, ResourceSellInfo sellInfo) {
        ResourceUpdateTagsRequest tagReq = new ResourceUpdateTagsRequest();
        tagReq.setResourceId(resourceId);
        tagReq.setGroupId(sellInfo.getGroupId());
        tagReq.setTagIds(List.of(sellInfo.getTagId()));
        resourceService.updateResourceTags(tagReq);
    }
}
