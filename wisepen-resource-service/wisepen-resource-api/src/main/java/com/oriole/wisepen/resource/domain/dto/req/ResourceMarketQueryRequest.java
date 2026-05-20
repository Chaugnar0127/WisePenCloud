package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.enums.ResourceSortBy;
import com.oriole.wisepen.resource.enums.SaleMethod;
import lombok.Data;

import java.util.List;

@Data
public class ResourceMarketQueryRequest {
    private String groupId;
    private List<String> tagIds;
    private String resourceType;
    private SaleMethod saleMethod;
    private ResourceSortBy sortBy = ResourceSortBy.UPDATE_TIME;
}
