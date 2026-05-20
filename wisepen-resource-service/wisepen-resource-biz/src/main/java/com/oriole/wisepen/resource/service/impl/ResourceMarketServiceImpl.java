package com.oriole.wisepen.resource.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceForkReqDTO;
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
import com.oriole.wisepen.resource.domain.dto.res.ResourceSellInfoResponse;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.resource.enums.OwnershipTier;
import com.oriole.wisepen.resource.enums.ResourceAction;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.enums.SaleMethod;
import com.oriole.wisepen.resource.exception.ResourceError;
import com.oriole.wisepen.resource.repository.CustomResourceItemRepository;
import com.oriole.wisepen.resource.repository.ResourceItemRepository;
import com.oriole.wisepen.resource.repository.TagRepository;
import com.oriole.wisepen.resource.service.IForkResService;
import com.oriole.wisepen.resource.service.IResourceMarketService;
import com.oriole.wisepen.resource.service.IResourceService;
import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import com.oriole.wisepen.user.api.feign.RemoteWalletService;
import com.oriole.wisepen.user.api.feign.RemoteUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceMarketServiceImpl implements IResourceMarketService {

    private final ResourceItemRepository resourceItemRepository;
    private final CustomResourceItemRepository customResourceItemRepository;
    private final TagRepository tagRepository;
    private final IForkResService forkResService;
    private final IResourceService resourceService;
    private final RemoteNoteService remoteNoteService;
    private final RemoteUserService remoteUserService;
    private final RemoteWalletService remoteWalletService;

    @Override
    public PageR<ResourceMarketItemResponse> listMarketResources(ResourceMarketQueryRequest req, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, req.getSortBy().getDbField());
        List<ResourceItemEntity> resources = customResourceItemRepository.findMarketResources(
                req.getGroupId(), req.getTagIds(), req.getResourceType(), req.getSaleMethod(), sort);

        List<ResourceMarketItemResponse> items = new ArrayList<>();
        for (ResourceItemEntity resource : resources) {
            matchingSellInfos(resource, req.getGroupId(), req.getSaleMethod()).stream()
                    .map(sellInfo -> toMarketItem(resource, sellInfo))
                    .forEach(items::add);
        }

        int fromIndex = Math.min(Math.max(page - 1, 0) * size, items.size());
        int toIndex = Math.min(fromIndex + size, items.size());
        PageR<ResourceMarketItemResponse> pageR = new PageR<>(items.size(), page, size);
        pageR.addAll(items.subList(fromIndex, toIndex));
        return pageR;
    }

    @Override
    public ResourceMarketDetailResponse getMarketDetail(String resourceId) {
        ResourceItemEntity resource = getResource(resourceId);

        ResourceMarketDetailResponse response = BeanUtil.copyProperties(resource, ResourceMarketDetailResponse.class);
        response.setOwnerInfo(resolveOwnerInfo(resource.getOwnerId()));
        response.setCurrentTags(resolveCurrentTags(resource));
        response.setCanResell(canResell(resource));
        response.setSellInfos(safeSellInfos(resource).stream()
                .filter(this::isPurchasable)
                .map(this::toSellInfoResponse)
                .toList());
        return response;
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
        Long buyerId = SecurityContextHolder.getUserId();
        ResourceItemEntity resource = getResource(req.getResourceId());
        ResourceSellInfo sellInfo = findSellInfo(resource, req.getSellId());

        if (!isPurchasable(sellInfo)) {
            throw new ServiceException(ResourceError.SELL_INFO_NOT_PURCHASABLE);
        }
        if (buyerId.toString().equals(resource.getOwnerId())) {
            throw new ServiceException(ResourceError.RESOURCE_PERMISSION_DENIED);
        }

        R<Void> settleResponse = remoteWalletService.settleInfoPointTrade(
                buyerId, Long.valueOf(resource.getOwnerId()), sellInfo.getPrice(), IdUtil.getSnowflakeNextId());
        if (settleResponse == null || settleResponse.getCode() != 200) {
            throw new ServiceException(ResourceError.RESOURCE_MARKET_TRADE_SETTLE_FAILED);
        }

        ResourcePurchaseResponse response = BeanUtil.copyProperties(sellInfo, ResourcePurchaseResponse.class);
        response.setResourceId(resource.getResourceId());
        response.setSellId(sellInfo.getSellId());
        response.setSaleMethod(sellInfo.getSaleMethod());

        String deliveredResourceId = deliverPurchase(resource, sellInfo, buyerId.toString());
        response.setDeliveredResourceId(deliveredResourceId);
        response.setLatestForkAllowed(sellInfo.getSaleMethod() == SaleMethod.SUBSCRIPTION);
        log.info("resource purchased resourceId={} sellId={} buyerId={} saleMethod={} deliveredResourceId={}",
                resource.getResourceId(), sellInfo.getSellId(), buyerId, sellInfo.getSaleMethod(), deliveredResourceId);
        return response;
    }

    @Override
    public String forkLatestBySubscription(ResourceSubscriptionForkRequest req) {
        Long currentUserId = SecurityContextHolder.getUserId();
        ResourceItemEntity resource = getResource(req.getResourceId());
        ResourceSellInfo sellInfo = findSellInfo(resource, req.getSellId());

        if (sellInfo.getSaleMethod() != SaleMethod.SUBSCRIPTION) {
            throw new ServiceException(ResourceError.SUBSCRIPTION_FORK_NOT_ALLOWED);
        }
        ResourceCheckPermissionResDTO permission = resourceService.checkPermission(ResourceCheckPermissionReqDTO.builder()
                .resourceId(resource.getResourceId())
                .userId(currentUserId)
                .groupRoles(SecurityContextHolder.getGroupRoleMap())
                .build());
        if (permission.getAllowedActions() == null || !permission.getAllowedActions().contains(ResourceAction.FORK)) {
            throw new ServiceException(ResourceError.RESOURCE_PERMISSION_DENIED);
        }

        String newResourceId = forkResService.forkSnapshot(ResourceForkReqDTO.builder()
                .resourceId(resource.getResourceId())
                .resourceType(resource.getResourceType())
                .newOwnerId(currentUserId.toString())
                .tier(OwnershipTier.ORIGINAL)
                .version(resolveListedVersion(resource))
                .build());
        log.info("subscription latest forked resourceId={} sellId={} userId={} newResourceId={}",
                resource.getResourceId(), sellInfo.getSellId(), currentUserId, newResourceId);
        return newResourceId;
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

    private List<ResourceSellInfo> matchingSellInfos(ResourceItemEntity resource, String groupId, SaleMethod saleMethod) {
        return safeSellInfos(resource).stream()
                .filter(this::isPurchasable)
                .filter(sellInfo -> Objects.equals(groupId, sellInfo.getGroupId()))
                .filter(sellInfo -> saleMethod == null || sellInfo.getSaleMethod() == saleMethod)
                .toList();
    }

    private boolean isPurchasable(ResourceSellInfo sellInfo) {
        return !Boolean.TRUE.equals(sellInfo.getOffShelf())
                && sellInfo.getAdmin() != null
                && Boolean.TRUE.equals(sellInfo.getAdmin().getApproved());
    }

    private String deliverPurchase(ResourceItemEntity resource, ResourceSellInfo sellInfo, String buyerId) {
        if (sellInfo.getSaleMethod() == SaleMethod.USE_RIGHT) {
            resourceService.grantUserResourceActions(resource.getResourceId(), buyerId,
                    List.of(ResourceAction.DISCOVER, ResourceAction.VIEW, ResourceAction.DOWNLOAD_WATERMARK));
            return resource.getResourceId();
        }
        if (sellInfo.getSaleMethod() == SaleMethod.OWNERSHIP) {
            return forkResService.forkSnapshot(ResourceForkReqDTO.builder()
                    .resourceId(resource.getResourceId())
                    .resourceType(resource.getResourceType())
                    .newOwnerId(buyerId)
                    .tier(OwnershipTier.ORIGINAL)
                    .version(sellInfo.getVersion())
                    .build());
        }
        if (sellInfo.getSaleMethod() == SaleMethod.SUBSCRIPTION) {
            String newResourceId = forkResService.forkSnapshot(ResourceForkReqDTO.builder()
                    .resourceId(resource.getResourceId())
                    .resourceType(resource.getResourceType())
                    .newOwnerId(buyerId)
                    .tier(OwnershipTier.ORIGINAL)
                    .version(sellInfo.getVersion())
                    .build());
            resourceService.grantUserResourceActions(resource.getResourceId(), buyerId,
                    List.of(ResourceAction.DISCOVER, ResourceAction.VIEW,
                            ResourceAction.DOWNLOAD_WATERMARK, ResourceAction.FORK));
            return newResourceId;
        }
        throw new ServiceException(ResourceError.RESOURCE_TYPE_UNSUPPORTED_FOR_SELL);
    }

    private boolean canResell(ResourceItemEntity resource) {
        List<String> originalEditorIds = resource.getOriginalEditorIds();
        return originalEditorIds != null && originalEditorIds.contains(resource.getOwnerId());
    }

    private ResourceMarketItemResponse toMarketItem(ResourceItemEntity resource, ResourceSellInfo sellInfo) {
        ResourceMarketItemResponse response = BeanUtil.copyProperties(resource, ResourceMarketItemResponse.class);
        BeanUtil.copyProperties(sellInfo, response);
        return response;
    }

    private ResourceSellInfoResponse toSellInfoResponse(ResourceSellInfo sellInfo) {
        ResourceSellInfoResponse response = BeanUtil.copyProperties(sellInfo, ResourceSellInfoResponse.class);
        if (sellInfo.getAdmin() != null) {
            response.setApproved(sellInfo.getAdmin().getApproved());
            response.setReviewComment(sellInfo.getAdmin().getComment());
        }
        return response;
    }

    private UserDisplayBase resolveOwnerInfo(String ownerId) {
        try {
            Long owner = Long.valueOf(ownerId);
            Map<Long, UserDisplayBase> userMap = remoteUserService.getUserDisplayInfo(List.of(owner)).getData();
            return userMap == null ? null : userMap.get(owner);
        } catch (Exception e) {
            log.warn("market ownerInfo degraded ownerId={}", ownerId, e);
            return null;
        }
    }

    private Map<String, String> resolveCurrentTags(ResourceItemEntity resource) {
        Set<String> tagIds = safeSellInfos(resource).stream()
                .filter(this::isPurchasable)
                .map(ResourceSellInfo::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (tagIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> tagMap = new HashMap<>();
        tagRepository.findAllById(tagIds).forEach(tag -> tagMap.put(tag.getTagId(), tag.getTagName()));
        return tagMap;
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
