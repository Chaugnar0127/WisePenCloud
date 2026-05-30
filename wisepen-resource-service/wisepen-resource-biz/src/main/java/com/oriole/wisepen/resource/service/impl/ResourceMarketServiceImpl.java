package com.oriole.wisepen.resource.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.IdUtil;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import com.oriole.wisepen.note.api.feign.RemoteNoteService;
import com.oriole.wisepen.resource.domain.ResourceSellInfo;
import com.oriole.wisepen.resource.domain.ResourceSellInfos;
import com.oriole.wisepen.resource.domain.SellReviewInfo;
import com.oriole.wisepen.resource.domain.dto.req.ResourceForkRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourcePublishSellRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourcePurchaseRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceReviewSellRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceMarketDetailResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourcePurchaseResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceSellInfoResponse;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.resource.enums.ResourceAction;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.enums.SaleMethod;
import com.oriole.wisepen.resource.exception.ResourceError;
import com.oriole.wisepen.resource.repository.ResourceItemRepository;
import com.oriole.wisepen.resource.service.IResourceMarketService;
import com.oriole.wisepen.resource.util.MarketOrderIds;
import com.oriole.wisepen.resource.service.IResourceMarketTradeService;
import com.oriole.wisepen.resource.service.IResourceService;
import com.oriole.wisepen.resource.service.ITagService;
import com.oriole.wisepen.user.api.enums.InfoPointTradeReverseReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceMarketServiceImpl implements IResourceMarketService {

    private final ResourceItemRepository resourceItemRepository;
    private final ITagService tagService;
    private final IResourceService resourceService;
    private final RemoteNoteService remoteNoteService;
    private final IResourceMarketTradeService marketTradeService;

    @Override
    public ResourceMarketDetailResponse getMarketDetail(String resourceId, String groupId) {
        ResourceItemEntity resource = resourceItemRepository.findById(resourceId)
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));

        ResourceMarketDetailResponse response = BeanUtil.copyProperties(resource, ResourceMarketDetailResponse.class);
        response.setOwnerInfo(resourceService.resolveOwnerDisplay(resource.getOwnerId()));

        List<ResourceSellInfo> purchasableInGroup = resource.getSellInfos().stream()
                .filter(sellInfo -> isPurchasable(sellInfo)
                        && Objects.equals(groupId, sellInfo.getGroupId()))
                .toList();

        response.setCurrentTags(tagService.resolveTagNames(purchasableInGroup.stream()
                .map(ResourceSellInfo::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())));

        response.setSellInfos(purchasableInGroup.stream()
                .map(sellInfo -> {
                    ResourceSellInfoResponse item = BeanUtil.copyProperties(sellInfo, ResourceSellInfoResponse.class);
                    if (sellInfo.getAdmin() != null) {
                        BeanUtil.copyProperties(sellInfo.getAdmin(), item, CopyOptions.create()
                                .setFieldMapping(Map.of("comment", "reviewComment")));
                    }
                    return item;
                })
                .toList());
        return response;
    }

    @Override
    public String publishSellInfo(ResourcePublishSellRequest req, Long userId) {
        String currentUserId = userId.toString();
        ResourceItemEntity resource = resourceItemRepository.findById(req.getResourceId())
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));

        // 市场上架必须由资源所有者本人发起，避免代他人资源提交售卖。
        resourceService.assertResourceOwner(resource.getResourceId(), currentUserId);

        List<String> editorIds = resource.getOriginalEditorIds();
        if (editorIds == null || !editorIds.contains(currentUserId)) {
            throw new ServiceException(ResourceError.SELL_PUBLISHER_NOT_ORIGINAL_EDITOR);
        }

        boolean duplicateSale = resource.getSellInfos().stream()
                .anyMatch(sellInfo -> Objects.equals(req.getGroupId(), sellInfo.getGroupId())
                        && !Boolean.TRUE.equals(sellInfo.getOffShelf()));
        if (duplicateSale) {
            throw new ServiceException(ResourceError.SELL_INFO_ALREADY_LISTED);
        }

        // 上架时固化资源版本，购买后 fork 的内容必须与审核通过版本一致。
        ResourceType resourceType = resource.getResourceType();
        Long listedVersion;
        if (resourceType == ResourceType.NOTE) {
            final R<NoteSnapshotResponse> noteResponse;
            try {
                noteResponse = remoteNoteService.getNoteLatestVersion(resource.getResourceId());
            } catch (Exception e) {
                log.warn("resolveNoteListedVersion feign failed resourceId={}", resource.getResourceId(), e);
                throw new ServiceException(ResourceError.NOTE_VERSION_RESOLVE_FAILED);
            }
            if (noteResponse == null || !Integer.valueOf(200).equals(noteResponse.getCode())) {
                log.warn("resolveNoteListedVersion note service error resourceId={} code={} msg={}",
                        resource.getResourceId(),
                        noteResponse == null ? null : noteResponse.getCode(),
                        noteResponse == null ? null : noteResponse.getMsg());
                throw new ServiceException(ResourceError.NOTE_VERSION_RESOLVE_FAILED);
            }
            NoteSnapshotResponse snapshot = noteResponse.getData();
            listedVersion = snapshot == null || snapshot.getVersion() == null ? 0L : snapshot.getVersion();
        } else if (resourceType != null && resourceType.isOffice()) {
            listedVersion = 0L;
        } else {
            throw new ServiceException(ResourceError.RESOURCE_TYPE_UNSUPPORTED_FOR_SELL);
        }

        ResourceSellInfo sellInfo = BeanUtil.copyProperties(req, ResourceSellInfo.class);
        BeanUtil.fillBeanWithMap(Map.of(
                "sellId", IdUtil.fastSimpleUUID(),
                "saleMethod", SaleMethod.COPY_SUBSCRIPTION,
                "version", listedVersion,
                "offShelf", false,
                "listedAt", LocalDateTime.now(),
                "admin", SellReviewInfo.builder().approved(null).build()
        ), sellInfo, false);

        resource.getSellInfos().add(sellInfo);
        resourceItemRepository.save(resource);
        log.info("resource sellInfo published resourceId={} sellId={} groupId={} saleMethod={}",
                resource.getResourceId(), sellInfo.getSellId(), sellInfo.getGroupId(), sellInfo.getSaleMethod());
        return sellInfo.getSellId();
    }

    @Override
    public void reviewSellInfo(ResourceReviewSellRequest req, Long reviewerId, IdentityType identityType,
                               Map<Long, GroupRoleType> groupRoles) {
        ResourceItemEntity resource = resourceItemRepository.findById(req.getResourceId())
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));

        ResourceSellInfo sellInfo = ResourceSellInfos.requireSellInfo(resource, req.getSellId());

        if (identityType != IdentityType.ADMIN) {
            Long sellGroupId = Long.valueOf(sellInfo.getGroupId());
            GroupRoleType role = groupRoles == null ? null : groupRoles.get(sellGroupId);
            if (role != GroupRoleType.ADMIN && role != GroupRoleType.OWNER) {
                throw new ServiceException(ResourceError.RESOURCE_PERMISSION_DENIED);
            }
        }

        SellReviewInfo reviewInfo = sellInfo.getAdmin();
        if (reviewInfo == null) {
            reviewInfo = new SellReviewInfo();
            sellInfo.setAdmin(reviewInfo);
        }
        BeanUtil.copyProperties(req, reviewInfo);
        BeanUtil.fillBeanWithMap(Map.of(
                "reviewerId", reviewerId.toString(),
                "reviewedAt", LocalDateTime.now()
        ), reviewInfo, false);

        resourceItemRepository.save(resource);
        log.info("resource sellInfo reviewed resourceId={} sellId={} approved={}",
                resource.getResourceId(), sellInfo.getSellId(), req.getApproved());

        if (Boolean.TRUE.equals(req.getApproved())) {
            resourceService.updatePersonalResourceTags(resource.getResourceId(), sellInfo.getGroupId(), List.of(sellInfo.getTagId()));
        }
    }

    @Override
    public ResourcePurchaseResponse purchaseProduct(ResourcePurchaseRequest req, Long buyerId) {
        ResourceItemEntity resource = resourceItemRepository.findById(req.getResourceId())
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));
        ResourceSellInfo sellInfo = ResourceSellInfos.requireSellInfo(resource, req.getSellId());
        if (!Objects.equals(req.getGroupId(), sellInfo.getGroupId())) {
            throw new ServiceException(ResourceError.SELL_INFO_NOT_FOUND);
        }

        // 只有审核通过且未下架的售卖项才允许购买。
        if (!isPurchasable(sellInfo)) {
            throw new ServiceException(ResourceError.SELL_INFO_NOT_PURCHASABLE);
        }
        if (buyerId.toString().equals(resource.getOwnerId())) {
            throw new ServiceException(ResourceError.SELF_PURCHASE_NOT_ALLOWED);
        }
        String buyerIdStr = buyerId.toString();
        if (sellInfo.getPurchasedBuyerIds().contains(buyerIdStr)) {
            throw new ServiceException(ResourceError.MARKET_PURCHASE_ALREADY_EXISTS);
        }
        Long orderId = MarketOrderIds.of(sellInfo.getSellId(), buyerIdStr);

        ResourceForkRequest forkReq = ResourceForkRequest.builder()
                .sourceResourceId(resource.getResourceId())
                .resourceName(resource.getResourceName())
                .resourceType(resource.getResourceType())
                .preview(resource.getPreview())
                .size(resource.getSize())
                .version(sellInfo.getVersion())
                .build();
        try {
            marketTradeService.chargeMarketOrder(buyerId, Long.valueOf(resource.getOwnerId()), sellInfo.getPrice(), orderId);
            // 扣款成功后先登记购买人，再授权并 fork；后续投递失败会在 catch 中冲正。
            sellInfo.getPurchasedBuyerIds().add(buyerIdStr);
            resourceItemRepository.save(resource);
            resourceService.grantUserResourceActions(resource.getResourceId(), buyerIdStr,
                    List.of(ResourceAction.DISCOVER, ResourceAction.VIEW,
                            ResourceAction.DOWNLOAD_WATERMARK, ResourceAction.FORK));
            resourceService.forkResource(forkReq, buyerIdStr, orderId, sellInfo.getSellId());
        } catch (RuntimeException ex) {
            log.error("market purchase delivery failed resourceId={} sellId={} orderId={}",
                    resource.getResourceId(), sellInfo.getSellId(), orderId, ex);
            if (sellInfo.getPurchasedBuyerIds().remove(buyerIdStr)) {
                resourceItemRepository.save(resource);
            }
            if (!marketTradeService.tryReversePaidTrade(orderId,
                    InfoPointTradeReverseReason.DELIVERY_FAILED, ex.getMessage())) {
                log.error("MARKET_TRADE_REVERSE_ALERT purchase delivery failed and reverse failed "
                                + "resourceId={} sellId={} orderId={} buyerId={}",
                        resource.getResourceId(), sellInfo.getSellId(), orderId, buyerId);
            }
            throw ex;
        }

        ResourcePurchaseResponse response = BeanUtil.copyProperties(sellInfo, ResourcePurchaseResponse.class);
        response.setResourceId(resource.getResourceId());
        response.setOrderId(orderId);

        log.info("resource purchased paid resourceId={} sellId={} buyerId={} orderId={} saleMethod={}",
                resource.getResourceId(), sellInfo.getSellId(), buyerId, orderId, sellInfo.getSaleMethod());
        return response;
    }

    private boolean isPurchasable(ResourceSellInfo sellInfo) {
        return !Boolean.TRUE.equals(sellInfo.getOffShelf())
                && sellInfo.getAdmin() != null
                && Boolean.TRUE.equals(sellInfo.getAdmin().getApproved());
    }
}
