package com.oriole.wisepen.market.api.domain.dto.req;

import com.oriole.wisepen.market.api.enums.SortType;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProductSearchRequest implements Serializable {
    private Long groupId;
    private String tagIds;
    private String keyword;
    private Long tradeContentType;
    private Long resourceType;
    private SortType sortBy;
}