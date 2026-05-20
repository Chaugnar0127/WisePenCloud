package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.domain.base.ResourceItemInfoBase;
import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResourceMarketDetailResponse extends ResourceItemInfoBase {
    private String resourceId;
    private UserDisplayBase ownerInfo;
    private Map<String, String> currentTags;
    private List<ResourceSellInfoResponse> sellInfos;
    private Boolean canResell;
}
