package com.oriole.wisepen.resource.domain;

import com.oriole.wisepen.resource.enums.PreviewType;
import com.oriole.wisepen.resource.enums.SaleMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceSellInfo {
    private String sellId;
    private String groupId;
    private String tagId;
    private Integer price;
    private SaleMethod saleMethod;
    private PreviewType previewType;
    private Long version;
    private Boolean offShelf;
    private LocalDateTime listedAt;
    private SellReviewInfo admin;
}
