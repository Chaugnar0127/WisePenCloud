package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.enums.PreviewType;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.enums.SaleMethod;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceMarketItemResponse {
    private String resourceId;
    private String sellId;
    private String resourceName;
    private ResourceType resourceType;
    private String ownerId;
    private String groupId;
    private String tagId;
    private Integer price;
    private SaleMethod saleMethod;
    private PreviewType previewType;
    private Long version;
    private LocalDateTime listedAt;
}
